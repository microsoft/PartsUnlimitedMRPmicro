# Deploy the infrastructure and application using az cli

These steps deploy the whole environment from your local machine using Docker, AKS, ACR and MongoDB via the az cli.

If you want to learn on how you can automate the deployment using DevOps practices, you can look at the [Hands on Labs section.](https://microsoft.github.io/PartsUnlimitedMRPmicro/hols/circleci.html)

Steps to follow:

1. Pre-requisite setup
2. Create the infrastructure
3. Prep the AKS cluster
4. Load the application CosmosDB data
5. Install the application suite

## 1. Pre-requisite setup

Change `READY_LOCATION` variable to the desired azure datacenter and optionally `READY_RG` and `READY_PATH` variables then execute the script below.

```bash
# Variables
export READY_RG=pumrpmicro
export READY_LOCATION=eastus2
export READY_PATH=~/temp/pumrpmicro

# Folder
rm -rf $READY_PATH
mkdir -p $READY_PATH

# Clone the code locally
git clone git@github.com:Microsoft/PartsUnlimitedMRPmicro.git
cd PartsUnlimitedMRPmicro

# Create a resource group
az group create -n $READY_RG -l $READY_LOCATION
```

If an SSH key or ServicePrincipal is not already available to use, create it using the steps below:

```bash
# ServicePrincipal Creation
echo "Creating ServicePrincipal for AKS Cluster.."
export SP_JSON=`az ad sp create-for-rbac --role="Contributor"`
export SP_NAME=`echo $SP_JSON | jq -r '.name'`
export SP_PASS=`echo $SP_JSON | jq -r '.password'`
export SP_ID=`echo $SP_JSON | jq -r '.appId'`
echo "Service Principal Name: " $SP_NAME
echo "Service Principal Password: " $SP_PASS
echo "Service Principal Id: " $SP_ID

# SSH Keys
ssh-keygen -f $READY_PATH/pumrpmicro -t rsa -N ''
```

## 2. Create an AKS Cluster, Azure Container Registry (ACR), and CosmosDB

Use the ssh key and service principal to create the infrastructure using the included ARM template deployed Azure portal or az cli.

<a href="https://portal.azure.com/#create/Microsoft.Template/uri/https://raw.githubusercontent.com/Microsoft/PartsUnlimitedMRPmicro/master/deploy/azuredeploy.json" target="_blank">
    <img src="http://azuredeploy.net/deploybutton.png"/>
</a>

<a href="http://armviz.io/#/?load=https://raw.githubusercontent.com/Microsoft/PartsUnlimitedMRPmicro/master/deploy/azuredeploy.json" target="_blank">
    <img src="http://armviz.io/visualizebutton.png"/>
</a>

OR

Modify the `./deploy/azuredeploy.parameters.json` file with the secrets and execute:

```bash
az group deployment create --resource-group="$READY_RG" --template-file ./deploy/azuredeploy.json --parameters @./deploy/azuredeploy.parameters.json
```

## 3. Prep the AKS cluster

This gets the kubeconfig, sets the required kubernetes secrets for the applications, and installs helm.

```bash
az aks get-credentials -g $READY_RG -n $READY_RG

# Add the secrets to be authenticated in the cluster
READY_ACR_PASSWORD=$(az acr credential show -n ${READY_RG}acr -g ${READY_RG} -o tsv --query 'passwords[0].value')

kubectl create secret docker-registry puregistrykey --docker-server=https://${READY_RG}acr.azurecr.io --docker-username=${READY_RG}acr --docker-password=$READY_ACR_PASSWORD --docker-email=$READY_RG@contoso.com

# Add the kubernetes CosmosDB secret for APIs to connect
kubectl create secret generic cosmosdb --from-literal=connection=$READY_COSMOSDB --from-literal=database=${READY_COSMOSDB_NAME}

# Install / Upgrade Helm
kubectl create serviceaccount --namespace kube-system tillersa
kubectl create clusterrolebinding tiller-cluster-rule --clusterrole=cluster-admin --serviceaccount=kube-system:tillersa
helm init --upgrade --service-account tillersa
```

## 4. Load the application CosmosDB data

Install the node.js mongodb npm package.
`npm install mongodb`

Execute `load_mock_data.js` with your DB information and run it to load your database with mock data.

Fill in the `READY_COSMOSDB_NAME` with the value from this query:
`az cosmosdb list -g ${READY_RG} --query [].name`

```bash
READY_COSMOSDB_NAME=

READY_COSMOSDB_PASS=$(az cosmosdb list-keys -n $READY_COSMOSDB_NAME -g ${READY_RG} -o tsv --query 'primaryMasterKey')

READY_COSMOSDB="mongodb://${READY_COSMOSDB_NAME}:${READY_COSMOSDB_PASS}@${READY_COSMOSDB_NAME}.documents.azure.com:10255/${READY_COSMOSDB_NAME}?ssl=true&replicaSet=globaldb"

node ./deploy/load_mock_data.js $READY_COSMOSDB_NAME $READY_COSMOSDB_PASS
```

The result will show:

```bash
Connected successfully to server
Records Imported
```

## 5. Install the application suite

Choose to install the application suite from the public docker hub images OR build and deploy the images to ACR.  Docker hub image installation path is the quickest while the ACR path allows deeper understanding of docker and ACR.

### a. Installation using Docker Hub and helm

```bash
helm install ./deploy/helm/pumrpmicro --name=web --set service.type=LoadBalancer,image.name=pumrp-web,image.repository=microsoft
helm install ./deploy/helm/pumrpmicro --name=order --set image.name=pumrp-order,image.repository=microsoft
helm install ./deploy/helm/pumrpmicro --name=catalog --set image.name=pumrp-catalog,image.repository=microsoft
helm install ./deploy/helm/pumrpmicro --name=shipment --set image.name=pumrp-shipment,image.repository=microsoft
helm install ./deploy/helm/pumrpmicro --name=quote --set image.name=pumrp-quote,image.repository=microsoft
helm install ./deploy/helm/pumrpmicro --name=dealer --set image.name=pumrp-dealer,image.repository=microsoft
```

### b. Installation using ACR

This allows the local machine to push docker images to ACR.

```bash
az acr login -n ${READY_RG}acr -g $READY_RG
```

This walks through doing a local build and push of each image to ACR.

### FrontEnd - Client

```bash
docker run --rm -v $PWD/Web:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./Web/Dockerfile --build-arg port=8080 -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-web:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-web:v1.0

helm install ./deploy/helm/pumrpmicro --name=client --set labels.tier=frontend,service.type=LoadBalancer,image.name=pumrp-web,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

### Backend - Order Service

```bash
docker run --rm -v $PWD/OrderSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./OrderSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-order:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-order:v1.0

helm install ./deploy/helm/pumrpmicro --name=order --set image.name=pumrp-order,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

### Backend - Catalog Service

```bash
docker run --rm -v $PWD/CatalogSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./CatalogSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-catalog:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-catalog:v1.0

helm install ./deploy/helm/pumrpmicro --name=catalog --set image.name=pumrp-catalog,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

### Backend - Shipment Service

```bash
docker run --rm -v $PWD/ShipmentSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./ShipmentSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-shipment:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-shipment:v1.0

helm install ./deploy/helm/pumrpmicro --name=shipment --set image.name=pumrp-shipment,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

### Backend - Quote Service

```bash
docker run --rm -v $PWD/QuoteSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./QuoteSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-quote:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-quote:v1.0

helm install ./deploy/helm/pumrpmicro --name=quote --set image.name=pumrp-quote,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

### Backend - Dealer Service

```bash
docker build --build-arg mongo_connection=$READY_COSMOSDB -f DealerService/Dockerfile -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-dealer:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-dealer:v1.0

helm install ./deploy/helm/pumrpmicro --name=dealer --set image.name=pumrp-dealer,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

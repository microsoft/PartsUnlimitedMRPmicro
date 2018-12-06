# Scripts to deploy the whole environment and the application using bash

Those steps allow you to deploy the whole environment from your local machine using Docker, AKS, ACR and MongoDB.

If you want to learn on how you can automate the deployment using DevOps practices, you can look at the [Hands on Labs section.](https://microsoft.github.io/PartsUnlimitedMRPmicro/hols/circleci.html)

## Setup prerequisites and variables

### Local Prerequisites

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

# SSH Keys
ssh-keygen -f $READY_PATH/pumrpmicro -t rsa -N ''

# Resource Group on Azure
az group create -n $READY_RG -l $READY_LOCATION
```

### ACR & AKS

```bash
az acr create -n ${READY_RG}acr -g $READY_RG --sku Basic --admin-enabled -l $READY_LOCATION

az acr login -n ${READY_RG}acr -g $READY_RG

echo "Creating ServicePrincipal for AKS Cluster.."
export SP_JSON=`az ad sp create-for-rbac --role="Contributor"`
export SP_NAME=`echo $SP_JSON | jq -r '.name'`
export SP_PASS=`echo $SP_JSON | jq -r '.password'`
export SP_ID=`echo $SP_JSON | jq -r '.appId'`
echo "Service Principal Name: " $SP_NAME
echo "Service Principal Password: " $SP_PASS
echo "Service Principal Id: " $SP_ID

echo "Retrieving Registry ID..."

ACR_ID="$(az acr show -n ${READY_RG}acr -g $READY_RG --query "id" --output tsv)"

echo "Registry Id:"$ACR_ID

echo "Granting Service Princpal " $SP_NAME " access to ACR..."
(
    set -x
    az role assignment create --assignee $SP_ID --role Reader --scope $ACR_ID
)

az aks create -g $READY_RG -n $READY_RG --ssh-key-value $READY_PATH/pumrpmicro.pub --node-count 3 -k 1.11.5 --client-secret $SP_PASS --service-principal $SP_ID -l $READY_LOCATION

az aks get-credentials -g $READY_RG -n $READY_RG

# To get the dashboard
# az aks browse -g $READY_RG -n $READY_RG

```

### MongoDB Backend using CosmosDB

```bash
READY_COSMOSDB_NAME=${READY_RG}db
az cosmosdb create -n $READY_COSMOSDB_NAME -g $READY_RG --kind MongoDB

READY_COSMOSDB_PASS=$(az cosmosdb list-keys -n $READY_COSMOSDB_NAME -g ${READY_RG} -o tsv --query 'primaryMasterKey')

READY_COSMOSDB="mongodb://${READY_COSMOSDB_NAME}:${READY_COSMOSDB_PASS}@${READY_COSMOSDB_NAME}.documents.azure.com:10255/${READY_COSMOSDB_NAME}?ssl=true&replicaSet=globaldb"
```

Install the node.js mongodb npm package.
`npm install mongodb`

Execute `load_mock_data.js` with your DB information and run it to load your database with mock data:

```shell
$ node ./deploy/load_mock_data.js $READY_COSMOSDB_NAME $READY_COSMOSDB_PASS
Connected successfully to server
Records Imported
```

### Prerequisites in the K8s cluster

```bash
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

## Application

These are the required steps to get the application and all microservices running.

### FrontEnd - Client

To simply deploy the latest tagged image from Docker hub:

```bash
helm install ./deploy/helm/pumrpmicro --name=web --set service.type=LoadBalancer,image.name=pumrp-web,image.repository=microsoft
```

**OR**
To build the image, push to ACR, and deploy the image from ACR:

```bash
docker run --rm -v $PWD/Web:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./Web/Dockerfile --build-arg port=8080 -t ${READY_RG}acr.azurecr.io/puclient/pumrp-web:v1.0 .

docker push ${READY_RG}acr.azurecr.io/puclient/pumrp-web:v1.0

helm install ./deploy/helm/pumrpmicro --name=client --set service.type=LoadBalancer,image.name=pumrp-web,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/puclient
```

> Note: The client is served on the `/mrp_client/` path.

### Backend - Order Service

To simply deploy the latest tagged image from Docker hub:

```bash
helm install ./deploy/helm/pumrpmicro --name=order --set image.name=pumrp-order,image.repository=microsoft
```

**OR**
To build the image, push to ACR, and deploy the image from ACR:

```bash
docker run --rm -v $PWD/OrderSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./OrderSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-order:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-order:v1.0

helm install ./deploy/helm/pumrpmicro --name=order --set image.name=pumrp-order,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

### Backend - Catalog Service

To simply deploy the latest tagged image from Docker hub:

```bash
helm install ./deploy/helm/pumrpmicro --name=catalog --set image.name=pumrp-catalog,image.repository=microsoft
```

**OR**
To build the image, push to ACR, and deploy the image from ACR:

```bash
docker run --rm -v $PWD/CatalogSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./CatalogSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-catalog:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-catalog:v1.0

helm install ./deploy/helm/pumrpmicro --name=catalog --set image.name=pumrp-catalog,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

### Backend - Shipment Service

To simply deploy the latest tagged image from Docker hub:

```bash
helm install ./deploy/helm/pumrpmicro --name=shipment --set image.name=pumrp-shipment,image.repository=microsoft
```

**OR**
To build the image, push to ACR, and deploy the image from ACR:

```bash
docker run --rm -v $PWD/ShipmentSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./ShipmentSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-shipment:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-shipment:v1.0

helm install ./deploy/helm/pumrpmicro --name=shipment --set image.name=pumrp-shipment,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

### Backend - Quote Service

To simply deploy the latest tagged image from Docker hub:

```bash
helm install ./deploy/helm/pumrpmicro --name=quote --set image.name=pumrp-quote,image.repository=microsoft
```

**OR**
To build the image, push to ACR, and deploy the image from ACR:

```bash
docker run --rm -v $PWD/QuoteSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./QuoteSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-quote:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-quote:v1.0

helm install ./deploy/helm/pumrpmicro --name=quote --set image.name=pumrp-quote,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

### Backend - Dealer Service

To simply deploy the latest tagged image from Docker hub:

```bash
helm install ./deploy/helm/pumrpmicro --name=quote --set image.name=pumrp-dealer,image.repository=microsoft
```

**OR**
To build the image, push to ACR, and deploy the image from ACR:

```bash
docker build --build-arg mongo_connection=$READY_COSMOSDB -f DealerService/Dockerfile -t ${READY_RG}acr.azurecr.io/pumrp/pumrp-dealer:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pumrp/pumrp-dealer:v1.0

helm install ./deploy/helm/pumrpmicro --name=dealer --set image.name=pumrp-dealer,image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pumrp
```

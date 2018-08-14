# Scripts to deploy the whole environment and the application using bash

Those steps allow you to deploy the whole environment from your local machine using Docker, AKS, ACR and MongoDB.

If you want to learn on how you can automate the deployment using DevOps practices, you can look at the [Hands on Labs section.](https://microsoft.github.io/PartsUnlimitedMRPmicro/hols/circleci.html)

## Setup prerequisites and variables

### Local Prerequisites

```bash
# Variables
export READY_RG=pumrpmicro
export READY_LOCATION=westus2
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

az aks create -g $READY_RG -n $READY_RG --ssh-key-value $READY_PATH/pumrpmicro.pub --node-count 3 --client-secret $SP_PASS --service-principal $SP_ID -l $READY_LOCATION

az aks get-credentials -g $READY_RG -n $READY_RG

# To get the dashboard
# az aks browse -g $READY_RG -n $READY_RG

```

### MongoDB Backend using CosmosDB

```bash
az cosmosdb create -n ${READY_RG}db -g $READY_RG --kind MongoDB

READY_COSMOSDB_TEMP=$(az cosmosdb list-connection-strings -n ${READY_RG}db -g ${READY_RG} -o tsv --query 'connectionStrings[0].[connectionString]')

READY_COSMOSDB=$(echo ${READY_COSMOSDB_TEMP/?ssl=true/pumrp?ssl=true})

# Old script to feed the DB
# https://raw.githubusercontent.com/Microsoft/PartsUnlimitedMRP/master/deploy/MongoRecords.js

# Connect to the db using the MongoShell
mongo readywinter18db.documents.azure.com:10255/purmp -u readywinter18db -p sPc4ex1MGoqQsquW35m3XVek3CuRMaFY31dIZlMFvEkcJiVH0bU55PiroaZxNn6vdRgfusQmPJ17UdyqcIQcfA== --ssl --sslAllowInvalidCertificates
```

### Prerequisites in the K8s cluster

```bash
# Install / Upgrade Helm
helm init --upgrade

```

## Application

### Prometheus and Grafana

```bash
helm install ./deploy/helm/individual/prometheus --name=prometheus

helm install --name grafana stable/grafana --set server.service.type=LoadBalancer
```

### Cassandra

```bash
helm install ./deploy/helm/cassandra --name=cassandradbs
```

### Zipkin

```bash
docker run --rm -v $PWD/ZipkinServer:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./ZipkinServer/Dockerfile --build-arg port=9411 -t ${READY_RG}acr.azurecr.io/zipkin:v1.0 .

docker push ${READY_RG}acr.azurecr.io/zipkin:v1.0

helm install ./deploy/helm/individual/zipkinserver --name=zipkin --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/zipkin
```

### API Gateway

```bash
docker run --rm -v $PWD/RestAPIGateway:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build -x test

docker build -f ./RestAPIGateway/Dockerfile --build-arg port=9020 -t ${READY_RG}acr.azurecr.io/apigateway:v1.0 . 

docker push ${READY_RG}acr.azurecr.io/apigateway:v1.0

helm install ./deploy/helm/individual/apigateway --name=api --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/apigateway
```

### FrontEnd - Client

```bash
docker run --rm -v $PWD/Web:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./Web/Dockerfile --build-arg port=8080 -t ${READY_RG}acr.azurecr.io/puclient:v1.0 .

docker push ${READY_RG}acr.azurecr.io/puclient:v1.0

helm install ./deploy/helm/individual/partsunlimitedmrp --name=client --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/puclient
```

### Backend - Order Service

```bash
docker run --rm -v $PWD/OrderSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./OrderSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/puorder:v1.0 .

docker push ${READY_RG}acr.azurecr.io/puorder:v1.0

helm install ./deploy/helm/individual/orderservice --name=order --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/puorder
```

### Backend - Catalog Service

```bash
docker run --rm -v $PWD/CatalogSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./CatalogSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pucatalog:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pucatalog:v1.0

helm install ./deploy/helm/individual/catalogservice --name=catalog --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pucatalog
```

### Backend - Shipment Service

```bash
docker run --rm -v $PWD/ShipmentSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./ShipmentSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pushipment:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pushipment:v1.0

helm install ./deploy/helm/individual/shipmentservice --name=shipment --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pushipment
```

### Backend - Quote Service

```bash
docker run --rm -v $PWD/QuoteSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./QuoteSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/puquote:v1.0 .

docker push ${READY_RG}acr.azurecr.io/puquote:v1.0

helm install ./deploy/helm/individual/quoteservice --name=quote --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/puquote
```

### Backend - Dealer Service

```bash
docker build --build-arg mongo_connection=$READY_COSMOSDB --build-arg mongo_database=purmp -f DealerService/Dockerfile -t ${READY_RG}acr.azurecr.io/pudealer:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pudealer:v1.0

helm install ./deploy/helm/individual/dealerservice --name=dealer --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pudealer
```
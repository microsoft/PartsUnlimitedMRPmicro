# Scripts to deploy the whole environement and the application using bash

Those steps allow you to deploy the whole environement from your local machine using Docker, AKS, ACR and MongoDB.

## Setup prerequisites and variables

### Local Prerequisites

```bash
# Variables
export READY_RG=readywinter18
export READY_LOCATION=eastus
export READY_PATH=~/temp/ready

# Folder
rm -rf $READY_PATH
mkdir $READY_PATH
cd $READY_PATH

# SSH Keys
ssh-keygen -f readywint18 -t rsa -N ''

# Resource Group on Azure
az group create -n $READY_RG -l $READY_LOCATION
```

### AKS - K8s Cluster

```bash
# Create the AKS cluster using your AZURE_CLIENT_SECRET and AZURE_CLIENT_ID variables (You should set it before to run this script)
az aks create -g $READY_RG -n $READY_RG --ssh-key-value $READY_PATH/readywint18.pub --node-count 3 --client-secret $AZURE_CLIENT_SECRET --service-principal $AZURE_CLIENT_ID -l $READY_LOCATION

az aks get-credentials -g $READY_RG -n $READY_RG

# To get the dashboard
# az aks browse -g $READY_RG -n $READY_RG

```

### MongoDB Backen using CosmosDB

```bash
az cosmosdb create -n ${READY_RG}db -g $READY_RG --kind MongoDB

READY_COSMOSDB_TEMP=$(az cosmosdb list-connection-strings -n ${READY_RG}db -g ${READY_RG} -o tsv --query 'connectionStrings[0].[connectionString]')

READY_COSMOSDB=$(echo ${READY_COSMOSDB_TEMP/?ssl=true/pumrp?ssl=true})

# Old script to feed the DB
# https://raw.githubusercontent.com/Microsoft/PartsUnlimitedMRP/master/deploy/MongoRecords.js

# Connect to the db using the MongoShell
mongo readywinter18db.documents.azure.com:10255/purmp -u readywinter18db -p sPc4ex1MGoqQsquW35m3XVek3CuRMaFY31dIZlMFvEkcJiVH0bU55PiroaZxNn6vdRgfusQmPJ17UdyqcIQcfA== --ssl --sslAllowInvalidCertificates
```

### ACR - Private Registry

az acr create -n ${READY_RG}acr -g $READY_RG --sku Basic --admin-enabled -l $READY_LOCATION

az acr login -n ${READY_RG}acr -g $READY_RG

### Prerequisites in the K8s cluster

```bash
# Add the secrets to be authenticated in the cluster
kubectl create secret docker-registry puregistrykey --docker-server=https://${READY_RG}acr.azurecr.io --docker-username=${READY_RG}acr --docker-password=$READY_ACR_PASSWORD --docker-email=$READY_RG@contoso.com

# Install / Upgrade Helm
helm init --upgrade

# Store the ACR Password locally
READY_ACR_PASSWORD=$(az acr credential show -n ${READY_RG}acr -g ${READY_RG} -o tsv --query 'passwords[0].value')

# Clone the code locally
git clone git@github.com:Microsoft/PartsUnlimitedMRPmicro.git

cd PartsUnlimitedMRPmicro
```

## Application 

### Prometheus and Grafana

```bash
cd $READY_PATH

cd PartsUnlimitedMRPmicro

helm install ./deploy/helm/individual/prometheus --name=prometheus

helm install --name grafana stable/grafana --set server.service.type=LoadBalancer
```

### Cassandra

```bash
cd $READY_PATH

cd PartsUnlimitedMRPmicro

helm install ./deploy/helm/cassandra --name=cassandradbs
```

### Zipkin

```bash
cd $READY_PATH

cd PartsUnlimitedMRPmicro

docker run --rm -v $PWD/ZipkinServer:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./ZipkinServer/Dockerfile --build-arg port=9411 -t ${READY_RG}acr.azurecr.io/zipkin:v1.0 .

docker push ${READY_RG}acr.azurecr.io/zipkin:v1.0

helm install ./deploy/helm/individual/zipkinserver --name=zipkin --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/zipkin
```

### API Gateway

```bash
cd $READY_PATH

cd PartsUnlimitedMRPmicro

docker run --rm -v $PWD/RestAPIGateway:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build -x test

docker build -f ./RestAPIGateway/Dockerfile --build-arg port=9020 -t ${READY_RG}acr.azurecr.io/apigateway:v1.0 . 

docker push ${READY_RG}acr.azurecr.io/apigateway:v1.0

helm install ./deploy/helm/individual/apigateway --name=api --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/apigateway
```

### FrontEnd - Client

```bash
cd $READY_PATH

cd PartsUnlimitedMRPmicro

docker run --rm -v $PWD/Web:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./Web/Dockerfile --build-arg port=8080 -t ${READY_RG}acr.azurecr.io/puclient:v1.0 .

docker push ${READY_RG}acr.azurecr.io/puclient:v1.0

helm install ./deploy/helm/individual/partsunlimitedmrp --name=client --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/puclient
```

### Backend - Order Service

```bash
cd $READY_PATH

cd PartsUnlimitedMRPmicro

docker run --rm -v $PWD/OrderSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./OrderSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/puorder:v1.0 .

docker push ${READY_RG}acr.azurecr.io/puorder:v1.0

helm install ./deploy/helm/individual/orderservice --name=order --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/puorder
```

### Backend - Catalog Service

```bash
cd $READY_PATH

cd PartsUnlimitedMRPmicro

docker run --rm -v $PWD/CatalogSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./CatalogSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pucatalog:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pucatalog:v1.0

helm install ./deploy/helm/individual/catalogservice --name=catalog --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pucatalog
```

### Backend - Shipment Service

```bash
cd $READY_PATH

cd PartsUnlimitedMRPmicro

docker run --rm -v $PWD/ShipmentSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./ShipmentSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/pushipment:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pushipment:v1.0

helm install ./deploy/helm/individual/shipmentservice --name=shipment --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pushipment
```

### Backend - Quote Service

```bash
cd $READY_PATH

cd PartsUnlimitedMRPmicro

docker run --rm -v $PWD/QuoteSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build

docker build -f ./QuoteSrvc/Dockerfile --build-arg port=8080 --build-arg mongo_connection=$READY_COSMOSDB -t ${READY_RG}acr.azurecr.io/puquote:v1.0 .

docker push ${READY_RG}acr.azurecr.io/puquote:v1.0

helm install ./deploy/helm/individual/quoteservice --name=quote --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/puquote
```

### Backend - Dealer Service

```bash
cd $READY_PATH

cd PartsUnlimitedMRPmicro

docker build --build-arg mongo_connection=$READY_COSMOSDB --build-arg mongo_database=purmp -f DealerService/Dockerfile -t ${READY_RG}acr.azurecr.io/pudealer:v1.0 .

docker push ${READY_RG}acr.azurecr.io/pudealer:v1.0

helm install ./deploy/helm/individual/dealerservice --name=dealer --set image.tag=v1.0,image.repository=${READY_RG}acr.azurecr.io/pudealer
```
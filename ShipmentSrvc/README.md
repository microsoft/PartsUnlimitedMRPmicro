# Shipment Microservice

The shipment service is a spring java based micro service that provides options to create and track shipments for the fictional outsourced Manufacturing Resource Planning (MRP) application. The build process here creates a single archive (jar) file that is used by the Tomcat host for the service.

This microservice is also available as a [docker image on Docker hub](https://hub.docker.com/r/microsoft/pumrp-shipment/).

Below, learn how to:

[Build the service](#build-the-shipment-service)  
[Run the service](#run-the-service)  
[Clean all builds](#clean-all-builds)  
[See Unit Tests](#unit-testing)

## Build the Shipment Service

Below are instructions on how to build the shipment service using Windows, Linux, or Docker.

1. Windows

    Building on Windows the following command is used to build the shipment JAR file.

    ```command
    .\gradlew.bat build
    ```

1. Linux

    Building on Linux the following command is used to build the shipment JAR file.

    ```bash
        # ensure the 'gradlew' is executable
        chmod +x gradlew

        # now build
        ./gradlew build
    ```

1. Docker  
    This describes how to build the image using a publicly available docker image which has gradle installed.  

    From the root of the repository, execute:

    ```shell
    docker run --rm -v $PWD/ShipmentSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build
    ```

Any of above steps creates the ```orderservice-1.0.jar```  build in the ```./build/libs``` directory.

## Run the service

After the build has successfully completed, now it is possible to run the application which creates an available API for the shipment service.  The microservice requires a connection to a MongoDB or CosmosDB (using MongoDB API).

1. Obtain the connection string to your database.

    A MongoDB or [CosmosDB using MongoDB connection string](https://docs.microsoft.com/azure/cosmos-db/connect-mongodb-account) is required for the API to run and must be passed into the image.

    Be sure to insert any database name (lowercase, alphanumeric only) after the base connection url.  For instance with CosmosDB string:  
    `mongodb://mydbserver:longpassword@mydbserver.documents.azure.com:10255/?ssl=true&replicaSet=globaldb`  
    would add `pumrp` and now the connection string becomes:  
    `mongodb://mydbserver:longpassword@mydbserver.documents.azure.com:10255/pumrp?ssl=true&replicaSet=globaldb` 

1. Now run the API

   With Java 8 JRE installed locally, execute the following from the ./ShipmentSrvc directory. Replace the `<mongodb-string>` in the command below with your own to run the microservice locally on port 8080.  
   ```
   java -Dspring.data.mongodb.uri=<mongodb-string> -jar -Dserver.port=8080 ./build/libs/*.jar
   ```

    OR use a java docker image to run the jar via this command:

    ```
    docker run -p 8080:8080 -it --rm -v $PWD/ShipmentSrvc/build/libs:/usr/local/app/ -w /usr/local/app openjdk:8-jre "java -Dspring.data.mongodb.uri=<mongodb-string> -jar -Dserver.port=8080 *.jar"
    ```

## Clean all builds

If you'd like to remove all of the local builds execute the following on Windows or Linux.

1. Windows

    Run the following from the command prompt: 
    ```
    removeBuild.bat
    ``` 
    to remove the ```./build``` directory.

1. Linux

    On Linux remove the ```./build``` with the following command to 'clean'

    ```bash
    rm -rf ./build
    ```

## Unit Testing

For unit testing of shipment service, JUnit test cases are present in ```/src/test/java/smpl/shipment/ShipmentControllerTest.java``` file. These test cases are executed as a part of build process to verify the basic CRUD operations. Once test cases are executed, you can view test results at ```./build/reports/tests/index.html.```
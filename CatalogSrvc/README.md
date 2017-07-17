# Catalog Microservice

The catalog microservice is an API created using java spring connecting to MongoDB or CosmosDB and provides a catalog of inventory for the fictional outsourced Manufacturing Resource Planning (MRP) application. The build process creates a single archive (jar) file that is used by the Tomcat host for the service.

This microservice is also available as a [docker image on Docker hub](https://hub.docker.com/r/microsoft/pumrp-catalog/).

Below, learn how to:

[Build the service](#Build-the-Catalog-Service)
[Clean all builds](#Clean-all-builds)
[Unit Tests](#Unit-Testing)

## Build the Catalog Service

Below are instructions on how to build the catalog service using Windows, Linux, or Docker.

1. Windows

    Building on Windows the following command is used to build the catalog JAR file.

    ```command
    .\gradlew.bat build
    ```

1. Linux

    Building on Linux the following command is used to build the catalog JAR file.

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
    docker run --rm -v $PWD/OrderSrvc:/project -w /project --name gradle gradle:3.4.1-jdk8-alpine gradle build
    ```

Any of above steps creates the ```catalogservice-1.0.jar```  build in the ```./build/libs``` directory.

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

For unit testing of catalog service, JUnit test cases are present in ```/src/test/java/smpl/catalog/CatalogControllerTest.java``` file. These test cases are executed as a part of build process to verify the basic CRUD operations. Once test cases are executed, you can view test results at ```./build/reports/tests/index.html.```
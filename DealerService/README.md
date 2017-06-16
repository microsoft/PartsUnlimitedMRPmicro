# Building the Dealer Service #

The MRP Dealer service is a .Net Core based micro service that provides a dealer of inventory for the fictional outsourced Manufacturing Resource Planning (MRP) application. The build process here creates a single archive (zip) file when ran using CI and bin folder when ran manually.

## Running Code Natively ##

To Run code on local machine below steps need to be followed:

Install the dotnet SDK 1.1.2 using [link](https://github.com/dotnet/core/blob/master/release-notes/download-archives/1.1.2-download.md) and follow the steps as it is.

1. Copy the code from this GIT repository
1. Modify the /DealerService/appsettings.json file with your own MongoDB connection string
1. Go to the /DealerService folder and run the below commands

```
dotnet restore
dotnet build
dotnet run
```

## Building Image ##

To build the docker image, execute the following command from the root of the repository:
```
docker build -f DealerService/Dockerfile -t registry_name/dealerservice:1.0 .
```

Further this image can be pushed to a docker registry (such as Azure Container Registry) and used in helm charts to deploy to Kubernetes.

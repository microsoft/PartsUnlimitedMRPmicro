---
layout: page
title: Deploy to Azure Container Service with Kubernetes using Helm
category: HOLs
order: 1
---

Learn how to create and setup a new Azure Container Service (ACS) with Kubernetes (K8s) orchestrator, build the order microservice Docker image and push it to Azure Container Registry (ACR), and then deploy that microservice to the ACS K8s cluster using a Helm chart.

### Prerequisites

- Fundamental knowledge of how to use git version control.
- Create an Azure Container Registry (ACR) account. To understand how to create, [see here.](https://docs.microsoft.com/azure/container-registry/container-registry-get-started-portal)
    > Note: Other docker registries are supported, but steps given in this article will be a reference to ACR only.
- Build files for each the microservices - This is required to create valid docker images to deploy to the Kubernetes cluster.  The only exception is the [orderservice.jar build file](https://github.com/Microsoft/PartsUnlimitedMRPmicro/blob/master/deploy/docker/Order/orderservicemsa-0.9.1.jar) is checked into source for ease of the ability to go through the exact HOL reference steps below and build the order service docker image.  Generating build files for each of the services is not required _if_ it is planned to go through the VSTS CI/CD/RM HOL since the builds will be generated through those steps.

### Tasks Overview

1. Setup ACS Kubernetes Cluster
1. Create DocumentDB and use DocumentDB api for MongoDB
1. Build and Push Docker Container to ACR
1. Install and Setup Helm
1. Create Helm charts
1. Deploy Parts Unlimited MRP using Helm


## Task 1: Setup ACS Kubernetes Cluster

Create Kubernetes cluster using Azure container service cluster and connect to Kubernetes cluster

**Step 1.** Follow [Create and connect to Kubernetes cluster](https://docs.microsoft.com/azure/container-service/container-service-kubernetes-walkthrough) document to create an ACS Kubernetes Cluster.

The following screenshots and steps are provided for reference with respect to Parts Unlimited MRP project deployment.

```bash
# Create Azure Resource group
az group create --name=kube-res --location=eastasia

# Create Kubernetes cluster
az acs create --orchestrator-type=kubernetes --resource-group=kube-res --name=kube-container --dns-prefix=kube --agent-count=2
```

![](<../assets/deployacsk8shelm/task1step1newacscluster.png>)
*The script, while running will have a black screen and would take several minutes to complete. On completion, the script output should be similar to the above. If desired, check in Azure portal while the script is running to see resources being created. The above command creates a cluster named “kube-container” under “kube-res” resource group with two agents and one master.*

Check connectivity to the newly created ACS cluster by running

```bash
kubectl cluster-info
```

![](<../assets/deployacsk8shelm/task1step1kubectlclusterinfo.png>)
The above output tells that the local kubectl is able to communicate successfully with the kubernetes cluster. For more information or troubleshooting, see [Connect to an Azure Container Service cluster](https://docs.microsoft.com/en-us/azure/container-service/container-service-connect) and [using the Kubernetes web UI with Azure Container Service](https://docs.microsoft.com/en-us/azure/container-service/container-service-kubernetes-ui).

## Task 2:  Create DocumentDB and use DocumentDB api for MongoDB

This lab uses DocumentDB database as the data store and uses MongoDB connection string to connect to it.

[Follow the steps in this website](https://docs.microsoft.com/en-us/azure/documentdb/documentdb-connect-mongodb-account) which walks through the setup steps for DocumentDB and obtaining the MongoDB connection string.

## Task 3:  Build the Docker Image and push to Azure Container Registry

The code needs to be containerized using Docker in order to eventually be able to be deployed to a Kubernetes cluster.
How to dockerize your application is beyond the scope of this post, but check out this article on [dockerizing the Parts Unlimited MRP application](https://microsoft.github.io/PartsUnlimitedMRP/adv/adv-21-Docker.html) and also included the [Dockerfile to be used.](https://dxdevop.visualstudio.com/mrpmicro/_git/code?fullScreen=false&path=%2Fdeploy%2Fdocker%2FOrder%2FDockerfile&version=GBmaster&_a=contents)

**Step 1.** Clone the Parts Unlimited [repository](https://dxdevop.visualstudio.com/mrpmicro/_git/code).
Login to Docker Registry and Build the Docker Image

Navigate to the Order service Dockerfile on the cloned repository under deploy/docker/Order path, replace the `puregistry-on` name with the provisioned name from the prerequisite steps, and execute below commands:

```bash
docker login puregistry-on.azurecr.io –u puagent –p password \
docker build --build-arg port=8080 --build-arg mongo_connection=<Mongo Db connection string> -t
    puregistry-on.azurecr.io/puorder:v1.0 .
```

The mentioned command builds an image based on the current directory:
- The image name must match the URL of ACR, that is,
    myownname-on.azurecr.io/mypathtoorganize/imagename.\
    The dot at the end tells Docker build to look at current folder
    for Docker file
- This command takes port on which the service will be exposed and
    mongo db connection string as an argument.
- Kubernetes works well if the EXPOSE command is present in the
    Dockerfile for necessary communication ports the app requires to
    function.

**Step 2.** Push the Docker image to ACR

Replace the name of puregistry with the ACR name created in the prequisite steps and execute.
```bash
Docker push puregistry-on.azurecr.io/puorder:v1.0
```

For More Info on docker registry, please follow the following
[document](https://docs.microsoft.com/en-us/azure/container-registry/container-registry-get-started-docker-cli).

## Task 4:  Install and Setup Helm

Helm is a tool for managing Kubernetes charts. Charts are packages of pre-configured Kubernetes resources. A quick walkthrough on helm is available at: <https://github.com/kubernetes/helm>.

**Step 1.** Install Helm

**For Windows**

- Download the [required version](https://github.com/kubernetes/helm/releases)
- Unpack it by right clicking “helm-v2.2.3-windows-amd64.zip” file and extract to helm-v2.2.3-windows-amd64/
- Find the helm binary in the unpacked directory, and move it to its desired destination

**For Linux and OSX**

Helm has an installer script that will automatically grab the latest version of the Helm client and [install it locally](https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get)

```bash
curl https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get/_helm.sh
chmod 700 get\_helm.sh
./get\_helm.sh
```

Simply type `helm` to ensure helm is properly installed.  Helm should be installed path as system variable in environment variable

The [installation guide](https://github.com/kubernetes/helm/blob/master/docs/install.md) is provided for reference.

**Step 2.** Set up Helm

Once Helm is installed locally, initialize the local CLI and install Tiller to the Kubernetes cluster in one step:

`helm init`

![](<../assets/deployacsk8shelm/task4step2helminit.png>)

The command will install Tiller into the Kubernetes cluster created in step 1

The “Initialize Helm and Install Tiller” section is [here](https://github.com/kubernetes/helm/blob/master/docs/quickstart.md) for reference. To ensure Helm is installed locally and on the cluster run:

`helm version`

## Task 5:  Create Helm charts

Helm uses a packaging format called charts. A chart is a collection of files which describes a related set of Kubernetes resources. These charts are used for deploying something simple, like a pod, or something complex, like a full web app stack with HTTP servers, databases, and caches.

**Step 1.** Create a blank helm chart template

execute the command below to create a blank helm chart template.

`helm create myk8sservice --name myk8sservice`

Notice the default files which are created in the directory where the command was executed and compare these to the ones which have already been populated with specific information to be able to deploy the order service to the kubernetes cluster in the steps below.

**Step 2.** Understand the Orderservice Helm chart

A chart is an organized collection of files inside a directory. The directory name is the name of the chart. For example, a chart describing orderservice would be stored in the orderservice directory.

Inside this directory, Helm will expect a structure that matches the following structure:
>
> \$ orderservice  
> ├── Charts.yaml  
> ├── values.yaml  
> ├── templates  
> │ ├── _helper.tpl  
> │ ├── deployment.yaml  
> │ ├── Notes.txt  
> │ └── service.yaml

Let us quickly go through the functionality of each of the file and folder:
> **Charts.yaml:** It’s a YAML file containing information about the chart. These settings may include:

- apiVersion: It is version of helm.
- description: It provides a single line description of this project.
- Name: Name of the chart (upper case character is not allowed in name).
- Version: Every chart must have a version number. A version must follow the http://semver.org/[SemVer 2](http://semver.org/) standard. Kubernetes Helm uses version numbers as release markers. Packages in repositories are identified by name plus version.[SemVer2](http://semver.org/) standard.
- sources: A list of URLs to source code for this project (optional).
- maintainers: \# (optional)
- name: The maintainer's name (required for each maintainer).
- email: The maintainer's email (optional for each maintainer).
- engine: gotpl \# The name of the template engine (optional, defaults to gotpl).

> **values.yaml**: It declare variables to be passed into templates. Helm updates any service using this file’s values. It contains the following fields for use:

- replicaCount: How many replica counts of service you want
- image: The Image file to be used for deployment
- repository: Your private Docker registry/folder name of Docker image for service
- tag: latest
- pullPolicy: IfNotPresent
- service:
- name: Name of micro service
- type: mention if it is using ClusterIP or load balancer.
- portName: http
- externalPort: external port number
- internalPort: internal port number
- Resources: These resources values are by default present when the chart is created and can be updated if needed depending on the needs.

    cpu: 100m  
    memory: 128Mi
- Requests: These resources values are by default present when the chart is created and can be updated if needed depending on the needs.

    cpu: 100m  
    memory: 128Mi

> **Templates(folder)**: A directory of templates, when combined with values, will generate valid Kubernetes manifest files.

> **_helper.tpl**: (optional) This file can be used to have default fully qualified app names.

> **Deployment.yaml**: This file refers to values.yaml and overwrites values mentioned in values.yaml. Helm provides a default structure for this file and can update the values accordingly.

Example: In deployment.yaml, see the following field:
    `replicas: {{ .Values.replicaCount }}`

Now **Values.replicaCount** will get values from values.yaml file replicaCount parameter which will be used in deployment of service.

> **Notes.txt**: A plain text file containing short usage notes which is automatically displayed in the console after deployment.

> **Services.yaml**: same as deployment.yaml.

**Step 3.** Modify the Image Registry value for Azure Container Registry

Open the `/deploy/helm/individual/orderservice/values.yaml` file and modify the  `image.repository` puregistrykey with the name of your own Azure Container Registry.  
The private Docker registry must be mentioned where images are located for the micro services and is required for deployment to succeed. This parameter will be again used by deployment.yaml and services.yaml file during deployment.

**Step 4.** Create and deploy a pull secret to the ACS cluster

To create a Pull secret key, replace your own values for `puregistry-on`, `ACR_USERNAME`, `ACR_PASSWORD`, and `ANY_EMAIL_ADDRESS`, then run the following command:

```bash
kubectl create secret docker-registry puregistrykey --docker-server=https://puregistry-on.azurecr.io --docker-username=ACR_USERNAME --docker-password=ACR_PASSWORD --docker-email=ANY_EMAIL_ADDRESS
```

Refer to [using helm to manage charts](https://github.com/kubernetes/helm/blob/master/docs/charts.md#using-helm-to-manage-charts) for more information.

## Task 6: Deploy Parts Unlimited MRP using Helm

**Step 1.** Once the Helm charts are ready using above command, run the following helm install command to deploy microservices. Make sure the command path is in same location where all the charts are (`/deploy/helm/individual/`).

`helm install orderservice --name=order --set image.tag=v1.0`

![](<../assets/deployacsk8shelm/task6step1helminstall.png>)

**Step 2.** To check if service is deployed use the following command:

`helm list`

![](<../assets/deployacsk8shelm/task6step2helmlist.png>)

> **Note:** It is possible to combine install and deployment of these microservices into a single helm chart.  This has been done already under the `deploy\helm\partsunlimitedmrp\` directory for reference or for an easier dev/test deployment.  However it is best DevOps practice and microservice principle to enable independent deployment pipelines and have separate charts for each microservice.

At any point of time if there is any syntax error in any of the *.yaml file then helm command would fail giving an exception.
    Example: If there is a spelling mistake in the yaml file, then while trying to deploy it will create an error as shown:
    ![](<../assets/deployacsk8shelm/task6stepfinalhelmerror.png>)

## Conclusion

This document covered how to create and setup a new Azure Container Service with Kubernetes orchestrator followed by building a new docker image and installing the order microservice to the cluster using helm. Some fundamentals should now be understood around helm and helm charts.

Here are some useful common helm commands:

- `helm ls`: Shows all running deployments.
- `helm ls -- all`: Shows all the running, failed deployments.
- `helm upgrade <releasename>`: To upgrade any service (ex: replicas parameter in any service) in deployment. Also, on reverting the changes and running this command again it will roll back the changes.
- `helm delete <releasename>`: Delete all resources deployed to the clsuter defined in the chart.
- `helm lint <chartname>`: Verifies that chart is valid and meeting the standard.
- `helm install <chart> --name=<releasename> --dry-run --debug`: To perform dry run and debug/review the generated kubernetes deployment before trying to actually deploy to the cluster.

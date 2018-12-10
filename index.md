---
layout: default
title: "PartsUnlimitedMRPmicro"
---

# Parts Unlimited MRP Microservices

Parts Unlimited MRP Microservices is a fictional outsourced Manufacturing Resource Planning (MRP) application for training purposes based on the description in chapters 31-35 of The Phoenix Projectby Gene Kim, Kevin Behr and George Spafford. © 2013 IT Revolution Press LLC, Portland, OR. Resemblance to “Project Unicorn” in the novel is intentional; resemblance to any real company is purely coincidental.

This microservices-based application uses entirely open source software including Docker, Kubernetes, Java, Apache, Hystrix, and MongoDB which creates a web front end and 5 supporting microservices. Click here for the related [Parts Unlimited Website application](http://microsoft.github.io/PartsUnlimited/) which is .Net Core and SQL Azure based or here for the same [Parts Unlimited MRP application](http://aka.ms/pumrplabs) except with everything running on a single Azure Linux VM (IaaS) without a microservices architecture.

## Key Features

- Entire application is dockerized and runs on Azure Container Service with Kubernetes orchestrator
- Front end website - runs Apache Tomcat and talks to all microservices services utilizing Hystrix and JSP.
- Catalog, Order, Shipment, and Quote microservices - are Java spring APIs and call DocDb using the MongoDB driver.
- Dealer microservice - is a .Net Core API using C#

## Hands On Labs

All of these labs utilize the application designed to run on top of Kubernetes.  Here are the recommended lab options for starting out:

1. [Deployment to Azure Kubernetes Service using Helm]({{ site.baseurl}}{% post_url 2017-04-24-deploy-aks-kubernetes-helm %}) - These steps deploy the entire application suite onto Azure Kubernetes Service (AKS) and dependent infrastructure (CosmosDB with MongoDB API and Azure Container Registry) using the `az` cli.
1. [CI with CircleCI and Azure Container Registry]({{ site.baseurl}}{% post_url 2017-11-13-circleci %}) - builds and tests all 6 docker images then deploys them to ACR using CircleCI

After completion of lab 1 essentially all services have been deployed to the Kubernetes cluster, then the following labs can be completed in any desired order:

- Implement Fluentd logging
- Implement Monitoring and Alerts with Prometheus
- Implement Opentracing using Zipkin

## Contributing

We truly appreciate your contributions to keep this sample application code and content updated! To contribute please fork this repository and issue a pull request to the designated branch (i.e. gh-pages for content, master for code). If you find problems or would like to see new things added/updated, please [submit an issue](https://github.com/Microsoft/PartsUnlimitedMRPmicro/issues).

### Legal

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). 
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

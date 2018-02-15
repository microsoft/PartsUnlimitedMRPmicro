# Parts Unlimited MRP Microservices

Parts Unlimited MRP Microservices is a fictional outsourced Manufacturing Resource Planning (MRP) application for training purposes based on the description in chapters 31-35 of The Phoenix Projectby Gene Kim, Kevin Behr and George Spafford. © 2013 IT Revolution Press LLC, Portland, OR. Resemblance to “Project Unicorn” in the novel is intentional; resemblance to any real company is purely coincidental.

This microservices-based application uses entirely open source software including Docker, Kubernetes, Java, Apache, Hystrix, and MongoDB which creates a web front end and 5 supporting microservices. Click here for the related [Parts Unlimited Website application](http://github.com/microsoft/partsunlimited) 
or here for the same [Parts Unlimited MRP application](http://aka.ms/pumrplabs) except with everything running on a single Azure Linux VM (IaaS) without a microservices architecture.

To read and learn more about this project, please visit the [documentation website](https://microsoft.github.io/PartsUnlimitedMRPmicro/).

CircleCI Build status: [![CircleCI](https://circleci.com/gh/Microsoft/PartsUnlimitedMRPmicro/tree/master.svg?style=svg)](https://circleci.com/gh/Microsoft/PartsUnlimitedMRPmicro/tree/master)

## Key Features

- Entire application is dockerized and runs on [Azure Container Service](https://docs.microsoft.com/azure/aks/) (AKS) or any other Kubernetes cluster
- Front end service - runs Apache Tomcat and talks to all microservices services utilizing Hystrix and JSP.
- Catalog, Order, Shipment, and Quote microservices - are Java spring APIs and call DocDb using the MongoDB driver.
- Dealer microservice - is a .Net Core API using C#
- Microservices and frontend container [images are available on Docker hub](https://hub.docker.com/r/microsoft/pumrp-web/).

## Contributing

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

FROM openjdk:8-jre

MAINTAINER david.tesar@microsoft.com

RUN mkdir -p /usr/local/app

WORKDIR /usr/local/app

COPY * /usr/local/app/

EXPOSE 8080

ENTRYPOINT java -Dspring.data.mongodb.uri=mongodb://holdevopsmongodb:8jVuAUdL5wDZutQ5r1FQR0zUEi276FpjdcZjOG6LIDX0DgKuTvOHForS0icrPjGmOv8Kfh60qNi0RWLDBKb80Q==@holdevopsmongodb.documents.azure.com:10250/ordering?ssl=true -jar -Dserver.port=8080 cat*.jar
#ENTRYPOINT ["java","-Dspring.data.mongodb.uri=mongodb://holdevopsmongodb:8jVuAUdL5wDZutQ5r1FQR0zUEi276FpjdcZjOG6LIDX0DgKuTvOHForS0icrPjGmOv8Kfh60qNi0RWLDBKb80Q==@holdevopsmongodb.documents.azure.com:10250/PartsUnlimitedDb?ssl=true", "-jar", "cat*.jar"]
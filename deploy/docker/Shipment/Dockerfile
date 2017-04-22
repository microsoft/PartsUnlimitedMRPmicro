FROM openjdk:8-jre

RUN mkdir -p /usr/local/app

WORKDIR /usr/local/app

COPY * /usr/local/app/

EXPOSE 8080

ENTRYPOINT java -Dspring.data.mongodb.uri=mongodb://mymongodb:mysecret@mydocdbname.documents.azure.com:10250/ordering?ssl=true -jar -Dserver.port=8080 shi*.jar
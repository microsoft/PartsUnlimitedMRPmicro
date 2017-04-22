FROM openjdk:8-jre

ARG port
ARG mongo_connection

ENV expose_port=$port
ENV mongo_conn=$mongo_connection

RUN mkdir -p /usr/local/app

WORKDIR /usr/local/app

COPY * /usr/local/app/

EXPOSE $expose_port

ENTRYPOINT java -Dspring.data.mongodb.uri=$mongo_conn -jar -Dserver.port=$expose_port ord*.jar
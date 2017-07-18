FROM openjdk:8-jre

ARG VCS_REF
ARG BUILD_DATE
ARG port
ARG mongo_connection

ENV expose_port=$port
ENV mongo_conn=$mongo_connection

LABEL com.microsoft.vcs-ref=$VCS_REF \
      com.microsoft.vendor="Microsoft" \
      com.microsoft.name="pumrp-shipment" \
      com.microsoft.url="https://hub.docker.com/r/microsoft/pumrp-shipment/" \
      com.microsoft.vcs-url="https://github.com/Microsoft/PartsUnlimitedMRPmicro/tree/master/ShipmentSrvc" \
      com.microsoft.build-date=$BUILD_DATE

RUN mkdir -p /usr/local/app

WORKDIR /usr/local/app

COPY ShipmentSrvc/build/libs/*.jar /usr/local/app/

EXPOSE $expose_port

ENTRYPOINT java -Dspring.data.mongodb.uri=$mongo_conn -jar -Dserver.port=$expose_port *.jar
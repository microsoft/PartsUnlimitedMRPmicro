FROM openjdk:8-jre

ARG VCS_REF
ARG BUILD_DATE
ARG port
ARG mongo_connection

ENV expose_port=$port
ENV mongo_conn=$mongo_connection

LABEL org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vendor="Microsoft" \
      org.label-schema.name="pumrp-catalog" \
      org.label-schema.url="https://hub.docker.com/r/microsoft/pumrp-catalog/" \
      org.label-schema.vcs-url="https://github.com/Microsoft/PartsUnlimitedMRPmicro/tree/master/CatalogSrvc" \
      org.label-schema.build-date=$BUILD_DATE

RUN mkdir -p /usr/local/app

WORKDIR /usr/local/app

COPY CatalogSrvc/build/libs/*.jar /usr/local/app/

EXPOSE $expose_port

ENTRYPOINT java -Dspring.data.mongodb.uri=$mongo_conn -jar -Dserver.port=$expose_port *.jar
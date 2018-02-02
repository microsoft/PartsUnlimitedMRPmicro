FROM openjdk:8-jre

ARG VCS_REF
ARG BUILD_DATE
ARG port

ENV expose_port=$port

LABEL org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vendor="Microsoft" \
      org.label-schema.name="pumrp-zipkin" \
      org.label-schema.url="https://hub.docker.com/r/microsoft/pumrp-zipkin/" \
      org.label-schema.vcs-url="https://github.com/Microsoft/PartsUnlimitedMRPmicro" \
      org.label-schema.build-date=$BUILD_DATE

RUN mkdir -p /usr/local/app

WORKDIR /usr/local/app

COPY ZipkinServer/build/libs/*.jar /usr/local/app/

EXPOSE $expose_port

ENTRYPOINT java -jar *.jar
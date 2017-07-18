FROM tomcat:9

ARG VCS_REF
ARG BUILD_DATE
ARG port

ENV expose_port=$port

LABEL com.microsoft.vcs-ref=$VCS_REF \
      com.microsoft.vendor="Microsoft" \
      com.microsoft.name="pumrp-client" \
      com.microsoft.url="https://hub.docker.com/r/microsoft/pumrp-shipment/" \
      com.microsoft.vcs-url="https://github.com/Microsoft/PartsUnlimitedMRPmicro" \
      com.microsoft.build-date=$BUILD_DATE

COPY /Web/build/libs/*.war /usr/local/tomcat/webapps/

EXPOSE $expose_port

ENTRYPOINT catalina.sh run
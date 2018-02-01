FROM tomcat:9

ARG VCS_REF
ARG BUILD_DATE
ARG port

ENV expose_port=$port

LABEL org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vendor="Microsoft" \
      org.label-schema.name="pumrp-client" \
      org.label-schema.url="https://hub.docker.com/r/microsoft/pumrp-client/" \
      org.label-schema.vcs-url="https://github.com/Microsoft/PartsUnlimitedMRPmicro" \
      org.label-schema.build-date=$BUILD_DATE

COPY Web/build/libs/*.war /usr/local/tomcat/webapps/

EXPOSE $expose_port

ENTRYPOINT catalina.sh run
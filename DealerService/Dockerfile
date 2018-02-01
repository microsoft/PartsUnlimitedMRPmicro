FROM microsoft/dotnet:1.1.2-sdk

ARG VCS_REF
ARG BUILD_DATE

ARG mongo_connection
ARG mongo_database

ENV mongo_database=$mongo_database
ENV mongo_conn=$mongo_connection

# Create directory for the app source code
RUN mkdir -p /DealerApi/Dealer
WORKDIR /DealerApi/Dealer

LABEL org.label-schema.vcs-ref=$VCS_REF \
      org.label-schema.vendor="Microsoft" \
      org.label-schema.name="pumrp-dealer" \
      org.label-schema.url="https://hub.docker.com/r/microsoft/pumrp-dealer/" \
      org.label-schema.vcs-url="https://github.com/Microsoft/PartsUnlimitedMRPmicro/tree/master/DealerService" \
      org.label-schema.build-date=$BUILD_DATE

# Copy the source and restore depdendencies
COPY /DealerService /DealerApi/Dealer
RUN ["dotnet", "restore"]
RUN apt-get update
RUN ["dotnet", "build"]

# Expose the port and start the app
EXPOSE 8080

ENTRYPOINT ["dotnet", "run"]


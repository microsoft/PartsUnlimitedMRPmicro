FROM microsoft/dotnet:1.1.2-sdk

ARG VCS_REF
ARG BUILD_DATE

# Create directory for the app source code
RUN mkdir -p /DealerApi/Dealer
WORKDIR /DealerApi/Dealer

LABEL com.microsoft.vcs-ref=$VCS_REF \
      com.microsoft.vendor="Microsoft" \
      com.microsoft.name="pumrp-dealer" \
      com.microsoft.url="https://hub.docker.com/r/microsoft/pumrp-dealer/" \
      com.microsoft.vcs-url="https://github.com/Microsoft/PartsUnlimitedMRPmicro/tree/master/DealerService" \
      com.microsoft.build-date=$BUILD_DATE

# Copy the source and restore depdendencies
COPY /DealerService /DealerApi/Dealer
RUN ["dotnet", "restore"]
RUN apt-get update
RUN ["dotnet", "build"]

# Expose the port and start the app
EXPOSE 8080

ENTRYPOINT ["dotnet", "run"]


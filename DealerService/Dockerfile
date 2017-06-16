FROM microsoft/dotnet:1.1.2-sdk

# Create directory for the app source code
RUN mkdir -p /DealerApi/Dealer
WORKDIR /DealerApi/Dealer

# Copy the source and restore depdendencies
COPY /DealerService /DealerApi/Dealer
RUN ["dotnet", "restore"]
RUN apt-get update && apt-get install -y tcpdump jq
RUN ["dotnet", "build"]

# Expose the port and start the app
EXPOSE 8080

ENTRYPOINT ["dotnet", "run"]


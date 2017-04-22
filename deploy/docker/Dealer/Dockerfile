FROM microsoft/dotnet:1.0-sdk-projectjson

RUN apt-get update
RUN apt-get install unzip

# Create directory for the app source code
RUN mkdir -p /DealerApi/Dealer
WORKDIR /DealerApi/Dealer

# Copy the source and restore depdendencies
COPY . /DealerApi/Dealer
RUN unzip DealerService.zip
#Not required since we are directly using the published folder
#RUN dotnet restore 

# Expose the port and start the app
EXPOSE 8080
ENTRYPOINT dotnet DealerService.dll
#CMD [ "dotnet", "run" ]


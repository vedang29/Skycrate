## BACKEND ##

# Base image
FROM debian:12-slim

# Metadata
LABEL maintainer="kshitijka"
LABEL version=1.0
LABEL description="Skycrate is a web based file management system that uses Hadoop as filesystem."

# Update & upgrade & install & rm
RUN apt-get update && apt-get upgrade -y && \
    apt-get install -y openjdk-17-jdk && \
    rm -rf /var/lib/apt/lists/* 

# Create non-root user
RUN useradd -s /bin/bash skycrateBack

# Create work dir
RUN mkdir /app
RUN chown -R skycrateBack:skycrateBack /app
COPY ./target/ /app
WORKDIR /app

# Switch user
USER skycrateBack

EXPOSE 8081

CMD ["java", "-jar", "/app/skycrateBackend-0.0.1-SNAPSHOT.jar"]

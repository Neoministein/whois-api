
# 1st stage, build the app
FROM maven:3.9.0-eclipse-temurin-17-alpine as build

#Install git
RUN apk add git
#RUN apt-get install openssh

WORKDIR /dependency/neoutil

## Download NeoUtil dependencies
RUN git clone https://github.com/Neoministein/NeoUtil.git /dependency/neoutil
RUN mvn clean install -Dmaven.test.skip



# Create a first layer to cache the "Maven World" in the local repository.
# Incremental docker builds will always resume after that, unless you update
# the pom
WORKDIR /dataEndpoint

ADD pom.xml .
RUN mvn package -Dmaven.test.skip

# Do the Maven build!
# Incremental docker builds will resume here when you change sources
ADD src src
RUN mvn package -DskipTests
RUN echo "done!"

# 2nd stage, build the runtime image
FROM eclipse-temurin:17.0.6_10-jre-alpine

RUN apk add whois

WORKDIR /usr/share/endpoint

# Copy the binary built in the 1st stage
COPY --from=build /dataEndpoint/target/whois-api.jar ./
COPY --from=build /dataEndpoint/target/libs ./libs

CMD ["java", "--enable-preview" ,"-Djdk.serialFilter=org.apache.http.impl.auth.*;java.util.*;!*", "-jar", "whois-api.jar" ]

EXPOSE 8090

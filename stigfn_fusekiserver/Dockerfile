FROM maven:3-jdk-11-openj9
LABEL org.opencontainers.image.authors="Melvin Chelli <melvin.chelli@dfki.de>"

LABEL Description="This image provides a fuskei server with stigFN functions built in for the stigLD demo"

COPY ./ /home/fuskei-server

RUN cd /home/fuskei-server \
    && mvn clean install


EXPOSE 3230

WORKDIR /home/fuskei-server/target/
ENTRYPOINT ["java","-jar","StigFN_Fuseki-1.0-SNAPSHOT.jar"]

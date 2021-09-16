FROM maven:3-jdk-11-openj9
MAINTAINER Melvin Chelli <melvin.chelli@dfki.de>

LABEL Description="This image runs the agents for the stigLD demo"

COPY ./ /home/agent-runner
RUN cd /home/agent-runner \
    && mvn clean install 

WORKDIR /home/agent-runner/target/
ENTRYPOINT ["java","-jar","AgentRunner-1.0-SNAPSHOT.jar"]


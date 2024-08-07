FROM alpine/git as clone
WORKDIR /apps
RUN git clone https://github.com/pedroth/Learning.git
RUN git clone https://github.com/pedroth/pedroEngine.git

FROM maven:latest as build 
WORKDIR /apps
COPY --from=clone /apps/Learning/ /apps/Learning
COPY --from=clone /apps/pedroEngine/ /apps/pedroEngine
RUN cd Learning/ && \
    mvn install -DskipTests
RUN cd pedroEngine/ && \
    mvn install -DskipTests 

FROM ubuntu:23.10
RUN apt-get update && apt-get install -y openjdk-20-jdk
ENV JAVA_HOME /usr/lib/jvm/openjdk-20-amd64
ENV PATH $PATH:$JAVA_HOME/bin

WORKDIR /apps
COPY --from=build /apps/Learning/target /apps/Learning/
COPY --from=build /apps/pedroEngine/target /apps/pedroEngine/
# CMD ["cd " , "run.sh"]

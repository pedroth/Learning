FROM alpine/git AS clone
WORKDIR /apps
RUN git clone https://github.com/pedroth/Learning.git
RUN git clone https://github.com/pedroth/pedroEngine.git

FROM maven:3.9.6-eclipse-temurin-11 AS build 
WORKDIR /apps
COPY --from=clone /apps/Learning/ /apps/Learning
COPY --from=clone /apps/pedroEngine/ /apps/pedroEngine
RUN cd Learning/ && \
    mvn install -DskipTests
RUN cd pedroEngine/ && \
    mvn install -DskipTests 

FROM eclipse-temurin:11-jre

WORKDIR /apps
COPY --from=build /apps/Learning/target /apps/Learning/
COPY --from=build /apps/pedroEngine/target /apps/pedroEngine/
# CMD ["cd " , "run.sh"]

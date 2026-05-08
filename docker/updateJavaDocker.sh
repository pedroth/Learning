# run on docker folder
docker build -t pedroth/java-apps:latest -t pedroth/java-apps:v1.0.3 . # update version
docker push pedroth/java-apps:latest

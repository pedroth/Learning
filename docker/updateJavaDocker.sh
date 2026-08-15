#!/usr/bin/env sh
set -eu

IMAGE=pedroth/java-apps
VERSION=${VERSION:-v1.0.12}

# Run this script from the docker folder.
docker build --build-arg CACHEBUST="$(date +%s)" -t "$IMAGE:latest" -t "$IMAGE:$VERSION" .
docker push "$IMAGE:latest"
docker push "$IMAGE:$VERSION"

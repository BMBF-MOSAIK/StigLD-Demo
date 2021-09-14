#!/bin/bash

THIS_DIR=$(cd "$(dirname -- "${BASH_SOURCE[0]}")" && pwd -P)

IMAGE_PREFIX=${IMAGE_PREFIX:-melzchelli/stigld-visualise}
IMAGE_TAG=${IMAGE_TAG:-latest}
IMAGE_NAME=${IMAGE_PREFIX}:${IMAGE_TAG}

cd "$THIS_DIR"
ARGS=()

set -xe
docker run -p 4200:4200 "${ARGS[@]}" \
       --name=stigld-visualise \
       --rm -ti "${IMAGE_NAME}" "$@"

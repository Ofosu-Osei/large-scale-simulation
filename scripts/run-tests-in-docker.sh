#!/bin/bash
mkdir coverage
#docker run -it --rm -v `pwd`/coverage:/coverage-out citest bash
docker run --rm -v "$(pwd)"/coverage:/coverage-out citest scripts/test.sh

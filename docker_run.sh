#!/bin/bash
docker run --rm -it -v ${PWD}:/root -p 4000:4000 jekyll

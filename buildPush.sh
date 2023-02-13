#!/bin/bash

docker build -f MinestomDockerfile --tag ctr.avrg.dev/nazuna/minestom:dev .
docker build -f VelocityDockerfile --tag ctr.avrg.dev/nazuna/proxy:dev .

docker push ctr.avrg.dev/nazuna/proxy:dev
docker push ctr.avrg.dev/nazuna/minestom:dev

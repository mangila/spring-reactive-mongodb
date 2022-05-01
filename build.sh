#!/bin/sh
./mvnw clean install && docker build -t mangila/spring-reactive-mongodb . && docker push mangila/spring-reactive-mongodb
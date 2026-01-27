#!/bin/sh
mvn clean package spring-boot:build-image
k3d images import authorization-server:0.0.1-SNAPSHOT -c k3s-default
docker exec k3d-k3s-default-server-0 crictl images | grep authorization-server
kubectl apply -f k8s/k8s.yaml
kubectl rollout restart deployment authorization-server

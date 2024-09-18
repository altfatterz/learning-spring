mvn clean package spring-boot:build-image
k3d images import vault-agent-sidecar-injector:0.0.1-SNAPSHOT -c my-k8s-cluster

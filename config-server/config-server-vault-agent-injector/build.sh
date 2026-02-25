mvn clean package spring-boot:build-image
k3d images import config-server-vault-agent-injector:0.0.1-SNAPSHOT
docker exec k3d-k3s-default-server-0 crictl images | grep vault

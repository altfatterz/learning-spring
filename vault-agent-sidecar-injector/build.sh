mvn clean package spring-boot:build-image
k3d images import vault-agent-sidecar-injector:0.0.1-SNAPSHOT -c k8s-cluster
docker exec k3d-k8s-cluster-server-0 crictl images | grep vault

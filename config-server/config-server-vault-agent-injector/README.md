## Config Server with Vault Agent Injector 

Vault Agent Injector docs: https://developer.hashicorp.com/vault/docs/platform/k8s/injector

### Create a Kubernetes cluster:

```bash
$ k3d cluster create
$ kubectl cluster-info
Kubernetes control plane is running at https://0.0.0.0:49315
CoreDNS is running at https://0.0.0.0:49315/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
Metrics-server is running at https://0.0.0.0:49315/api/v1/namespaces/kube-system/services/https:metrics-server:https/proxy

$ kubectl get nodes
NAME                       STATUS   ROLES                  AGE   VERSION
k3d-k3s-default-server-0   Ready    control-plane,master   34s   v1.31.5+k3s1
```

### Create the config ConfigMap

```bash
$ cd k8s
$ kubectl create configmap service-configs \
  --from-file=config-server-client-dev.properties \
  --from-file=config-server-client-prd.properties
 
```

### Build a docker image and import to k8s-cluster

```bash
./build.sh 
```

### Install config-server

```bash
$ kubectl apply -f k8s/k8s.yaml
```

### Test it

```bash
$ kubectl get pods 
NAME                                                 READY   STATUS    RESTARTS   AGE
config-server-vault-agent-injector-bc466f96f-b5rb4   2/2     Running   0          2m18s

$ kubectl exec -it config-server-vault-agent-injector-bc466f96f-b5rb4 -c busybox -- ls -l /app/data/config
lrwxrwxrwx    1 root     root            42 Feb 25 11:06 config-server-client-dev.properties -> ..data/config-server-client-dev.properties
lrwxrwxrwx    1 root     root            42 Feb 25 11:06 config-server-client-prd.properties -> ..data/config-server-client-prd.properties

$ kubectl port-forward svc/config-server-vault-agent-injector 8080:8080

# in another terminal 
$ http :8080/config-server-client/dev
$ http :8080/config-server-client/prd

```

TODO from here


## Using Vault Agent Sidecar Injector with Kubernetes authentication

#### Install Vault

```bash
$ helm repo add hashicorp https://helm.releases.hashicorp.com
$ helm repo update

# install Vault in development mode
$ helm install my-vault hashicorp/vault --set "server.dev.enabled=true"

$ kubectl get pod | grep my-vault
my-vault-0                                           1/1     Running   0          42s
my-vault-agent-injector-7b6f54669c-jcmp7             1/1     Running   0          42s

$ kubectl get svc | grep my-vault
my-vault                            ClusterIP   10.43.95.194    <none>        8200/TCP,8201/TCP   64s
my-vault-agent-injector-svc         ClusterIP   10.43.99.66     <none>        443/TCP             64s
my-vault-internal                   ClusterIP   None            <none>        8200/TCP,8201/TCP   64

```

### Access the Vault UI

```bash
$ kubectl port-forward svc/my-vault 8200:8200
# token is 'root', see logs: kubectl logs -f my-vault-0 
$ open http://localhost:8200/ui 
```

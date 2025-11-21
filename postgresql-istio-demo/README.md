## Postgresql with Istio

### Create a k8s cluster

```bash
$ k3d cluster create k8s-cluster
$ kubectl cluster-info
```

### Install PostgresSQL

```bash
$ helm install my-postgresql bitnami/postgresql -f postgresql-values.yaml
$ helm status my-postgresql
```

### Test connection to PostgreSQL

```bash
$ kubectl port-forward svc/my-postgresql 5432:5432
$ export POSTGRES_PASSWORD=$(kubectl get secret my-postgresql -o jsonpath="{.data.postgres-password}" | base64 -d)
$ PGPASSWORD="$POSTGRES_PASSWORD" psql --host localhost -U postgres -d postgres -p 5432
```

### Build and install postgresql-istio-demo to k8s cluster

```bash
$ mvn spring-boot:build-image
# import image into the cluster
$ k3d images import postgresql-istio-demo:0.0.1-SNAPSHOT -c k8s-cluster
# check loaded images
$ docker exec k3d-k8s-cluster-server-0 crictl images | grep postgresql-istio-demo
# install postgresql-istio-demo on the k8s cluster
$ kubectl apply -f k8s/k8s.yml
# test connection
$ kubectl run -it busybox --image=radial/busyboxplus:curl --restart=Never --rm -- curl postgresql-istio-demo:8080/customers\?lastName=Doe 
[{"id":1,"name":"John Doe"},{"id":2,"name":"Jane Doe"},{"id":4,"name":"John Doe"},{"id":5,"name":"Jane Doe"}
```

### Install Latest Istio

```bash
$ cd ~/apps
$ curl -L https://istio.io/downloadIstio | sh -
# at the time of writing this is istio-1.24.2
# put it into .zshrc
# export ISTIO_HOME=~/apps/istio-1.18.7
# export PATH="$ISTIO_HOME/bin:$PATH"
```

### Check Istio version

```bash
$ istioctl version
Istio is not present in the cluster: no running Istio pods in namespace "istio-system"
client version: 1.24.2
$ istioctl x precheck
✔ No issues found when checking the cluster. Istio is safe to install or upgrade!
  To get started, check out https://istio.io/latest/docs/setup/getting-started/
# install Istio
$ istioctl install -f $ISTIO_HOME/samples/bookinfo/demo-profile-no-gateways.yaml -y
# get the pods
$ kubectl get pods -n istio-system
NAME                      READY   STATUS    RESTARTS   AGE
istiod-57fc4b6cd6-6sgfc   1/1     Running   0          8m39s
$ istioctl version
client version: 1.24.2
control plane version: 1.24.2
data plane version: 1.24.2 (1 proxies)
```

### Deploy with envoy-proxy

```bash
$ kubectl apply -f k8s/k8s-with-istio.yml
$ kubectl get pods 
NAME                                     READY   STATUS    RESTARTS   AGE
my-postgresql-0                          1/1     Running   0          41m
postgresql-istio-demo-6667dcb4bd-k4kqj   2/2     Running   0          3m43s
# check istio-proxy logs
$ kubectl logs -f postgresql-istio-demo-6667dcb4bd-k4kqj -c istio-proxy
# test connection, works also with Istio
$ kubectl run -it busybox --image=radial/busyboxplus:curl --restart=Never --rm -- curl postgresql-istio-demo:8080/customers\?lastName=Doe
[{"id":1,"name":"John Doe"},{"id":2,"name":"Jane Doe"},{"id":4,"name":"John Doe"},{"id":5,"name":"Jane Doe"}]
```

### Controlling the injection policy

Namespace level - (`enabled` or `disabled`)

```bash
$ kubectl label namespace default istio-injection=enabled
$ kubectl get ns --show-labels
# remove label
$ kubectl label namespace default istio-injection-
```

Pod level - `sidecar.istio.io/inject` with `"true"` or `"false"`


### Add peer-authentication

```bash
$ kubectl apply -f peer-authentication.yml
$ kubectl get peerauthentication
NAME                  MODE     AGE
peer-authentication   STRICT   23m
$ kubectl run -it busybox --image=radial/busyboxplus:curl --restart=Never --rm -- sh
# this is failing, however the app can still access postgresql during startup why is that? 
$ curl -v postgresql-istio-demo:8080/customers\?lastName=Doe

```




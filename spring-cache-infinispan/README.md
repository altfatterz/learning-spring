
### Create k8s cluster


```bash
$ k3d cluster create k8s-cluster
$ kubectl cluster-info
$ kubectl get nodes
NAME                       STATUS   ROLES                  AGE   VERSION
k3d-k8s-cluster-server-0   Ready    control-plane,master   26s   v1.28.8+k3s1
```

### Install Infinispan

```bash
$ helm repo add openshift-helm-charts https://charts.openshift.io/
$ helm repo update
$ helm upgrade --install infinispan openshift-helm-charts/infinispan -f infinispan-values.yml
$ helm list
NAME      	NAMESPACE	REVISION	UPDATED                              	STATUS  	CHART           	APP VERSION
infinispan	default  	1       	2024-10-03 09:45:37.472693 +0200 CEST	deployed	infinispan-0.4.1	15.0 
$ kubectl get sts -o wide
NAME         READY   AGE   CONTAINERS   IMAGES
infinispan   1/1     39s   infinispan   quay.io/infinispan/server:15.0
```


### Get connection parameters:

```bash
$ kubectl get secret infinispan-generated-secret -o jsonpath="{.data.identities-batch}" | base64 -d
```

### Expose

```bash
$ kubectl port-forward svc/infinispan 11222:11222
$ open browser http://localhost:11222
```


### Monitor infinispan

```bash
$ watch kubectl top pod infinispan-0
```
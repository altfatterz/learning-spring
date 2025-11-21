
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

TODO view it in `Prometheus` / `Grafana`:

http://localhost:11222/metrics


### Cleanup

```bash
$ helm delete infinispan

These resources were kept due to the resource policy:
[Secret] infinispan-generated-secret

release "infinispan" uninstalled
```

### JMX monitoring

```bash
$ kubectl port-forward infinispan-0 5000:5000
$ jconsole 127.0.0.1:5000
```

Performance tuning infinispan:

https://infinispan.org/docs/stable/titles/tuning/tuning.html#performance-tuning_tuning

-Xmn : -Xmn sets the size of young generation.

JVM Heap memory (Hotspot heap structure)  in java consists of following elements>
- Young Generation
    1a) Eden,
    1b) S0 (Survivor space 0)
    1c) S1 (Survivor space 1)
- Old Generation (Tenured)
- Permanent Generation.

### CLI

```bash
$ kubectl exec -it infinispan-0 -- bin/cli.sh
$ connect -u admin -p changeme
# check who is the coordinator
$ describe
[infinispan-0-16501@infinispan//containers/default] > ls cache
books
___script_cache
# approximate_entries_in_memory is similar on each no 'coordinator' node
$ stats caches/books
{
  "time_since_start" : 102,
  "time_since_reset" : 102,
  "approximate_entries" : 848,
  "approximate_entries_in_memory" : 848,
  "approximate_entries_unique" : 289,
  "current_number_of_entries" : -1,
  "current_number_of_entries_in_memory" : -1,
  "off_heap_memory_used" : 0,
  "data_memory_used" : -1,
  "stores" : 848,
  "retrievals" : 849,
  "hits" : 0,
  "misses" : 849,
  "remove_hits" : 0,
  "remove_misses" : 0,
  "evictions" : 0,
  "average_read_time" : 0,
  "average_read_time_nanos" : 36634,
  "average_write_time" : 0,
  "average_write_time_nanos" : 0,
  "average_remove_time" : 0,
  "average_remove_time_nanos" : 0,
  "required_minimum_number_of_nodes" : 1
}
```

### Running within the cluster with `k8s` 

```bash
$ mvn clean package spring-boot:build-image
$ k3d images import spring-cache-infinispan:0.0.1-SNAPSHOT -c k8s-cluster 
$ docker exec k3d-k8s-cluster-server-0 crictl images | grep spring-cache-infinispan
$ kubectl apply -f k8s/k8s.yaml
```


### Create cache with Inifinispan CLI

```bash
$ kubectl exec -it infinispan-0 -- sh
$ cat <<EOF > cache-config.json
// copy here the content of example-cache-config.json
EOF
$ bin/cli.sh 
$ connect -u admin -p changeme
$ create cache c1
$ describe caches/c1 
$ cd caches/c1
$ put foo bar
$ get foo
bar
```

- other commands:

```bash
describe //containers/default
availability
rebalance
```

### Resources

https://github.com/infinispan/infinispan-helm-charts
https://github.com/infinispan/infinispan (Issues tracked [here](https://issues.redhat.com/projects/ISPN/issues/ISPN-14766?filter=allopenissues))
https://github.com/infinispan/infinispan-simple-tutorials/
https://infinispan.org/docs/stable/titles/cli/cli.html





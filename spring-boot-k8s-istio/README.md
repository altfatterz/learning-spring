## Spring Boot on K8S with Istio

### Install Latest Istio

```bash
$ cd ~/apps
$ curl -L https://istio.io/downloadIstio | sh -
# at the time of writing this is istio-1.24.2
# put it into .zshrc
# export ISTIO_HOME=~/apps/istio-1.24.2
# export PATH="$ISTIO_HOME/bin:$PATH"
```

### Create a k8s cluster

```bash
$ k3d cluster create k8s-cluster
$ kubectl cluster-info
```

### Check Istio version

```bash
$ istioctl version
Istio is not present in the cluster: no running Istio pods in namespace "istio-system"
client version: 1.24.2
$ istioctl x precheck
✔ No issues found when checking the cluster. Istio is safe to install or upgrade!
  To get started, check out https://istio.io/latest/docs/setup/getting-started/
$   
```

For now follow this tutorial: https://istio.io/latest/docs/setup/getting-started/#download

Later extend this.

### Core DNS issue:

```bash
$ kubectl apply -f dnsutils-pod.yaml
$ kubectl exec -it dnsutils -- sh
$ nslookup istiod.istio-system
Server:		10.43.0.10
Address:	10.43.0.10#53

Name:	istiod.istio-system.svc.cluster.local
Address: 10.43.3.85
# scale down core-dns deployment
$ kubectl scale deploy coredns --replicas=0 -n kube-system
$ nslookup istiod.istio-system
;; connection timed out; no servers could be reached
# services work but as soon as they are restarted istio-proxy cannot be up again
$ kubectl logs -f productpage-v1-c5b7f7dbc-rjscw -c istio-prox
2024-10-25T07:39:26.386465Z	error	cache	resource:default failed to sign: create certificate: rpc error: code = Unavailable desc = connection error: desc = "transport: Error while dialing: dial tcp: lookup istiod.istio-system.svc on 10.43.0.10:53: read udp 10.42.0.20:36520->10.43.0.10:53: read: connection refused"
# scale back up the coredns
$ kubectl scale deploy coredns --replicas=1 -n kube-system
2024-10-25T07:40:31.916469Z	info	xdsproxy	connected to delta upstream XDS server: istiod.istio-system.svc:15012	id=25
2024-10-25T07:40:31.932354Z	info	ads	ADS: new connection for node:1
2024-10-25T07:40:31.932794Z	info	ads	ADS: new connection for node:2
2024-10-25T07:40:32.146200Z	info	cache	generated new workload certificate	resourceName=default latency=213.74223ms ttl=23h59m59.853803931s
2024-10-25T07:40:32.146264Z	info	cache	Root cert has changed, start rotating root cert
2024-10-25T07:40:32.146533Z	info	cache	returned workload trust anchor from cache	ttl=23h59m59.853467999s
2024-10-25T07:40:32.146689Z	info	cache	returned workload trust anchor from cache	ttl=23h59m59.853311223s
2024-10-25T07:40:32.399387Z	info	Readiness succeeded in 8m4.604736098s
2024-10-25T07:40:32.399754Z	info	Envoy proxy is ready
```

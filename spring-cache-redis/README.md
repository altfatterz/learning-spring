#### Spring Cache With Redis

1. Start Redis

```bash
$ docker compose up -d
```

2. Access UI

```bash
open http://localhost:8001
```

3. Start application in IntelliJ `SpringCacheRedisApplication`

4. View cached `data` / `ttl`

```bash
$ redis-cli
$ 127.0.0.1:6379> dbsize
(integer) 2
127.0.0.1:6379> keys *
1) "books::isbn-4567"
2) "books::isbn-1234"
127.0.0.1:6379> get books::isbn-4567
"\xac\xed\x00\x05sr\x00+com.github.altfatterz.springcacheredis.Book\x9f1\x91\xde\xcfs\xa2\b\x02\x00\x02L\x00\x04isbnt\x00\x12Ljava/lang/String;L\x00\x05titleq\x00~\x00\x01xpt\x00\tisbn-4567t\x00\tSome book"
127.0.0.1:6379> ttl books::isbn-4567
(integer) 46
```

5. Look under the hood

`RedisCacheConfiguration`

```java
@Bean
RedisCacheManager cacheManager(CacheProperties cacheProperties, CacheManagerCustomizers cacheManagerCustomizers, ObjectProvider<org.springframework.data.redis.cache.RedisCacheConfiguration> redisCacheConfiguration, ObjectProvider<RedisCacheManagerBuilderCustomizer> redisCacheManagerBuilderCustomizers, RedisConnectionFactory redisConnectionFactory, ResourceLoader resourceLoader) {

        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.builder(redisConnectionFactory).cacheDefaults(this.determineConfiguration(cacheProperties, redisCacheConfiguration, resourceLoader.getClassLoader()));
        List<String> cacheNames = cacheProperties.getCacheNames();
        if (!cacheNames.isEmpty()) {
            builder.initialCacheNames(new LinkedHashSet(cacheNames));
        }

        if (cacheProperties.getRedis().isEnableStatistics()) {
            builder.enableStatistics();
        }

        redisCacheManagerBuilderCustomizers.orderedStream().forEach((customizer) -> {
            customizer.customize(builder);
        });
        return (RedisCacheManager)cacheManagerCustomizers.customize(builder.build());
    }
```

6. Stats

```bash
$ redis-cli INFO stats | grep keyspace
keyspace_hits:30
keyspace_misses:49

$ redis-cli INFO stats | grep expired
expired_subkeys:0
expired_keys:71
expired_stale_perc:1.37
expired_time_cap_reached_count:0
```

7. Redis on Kubernetes

```bash
$ k3d cluster create k8s-cluster
$ kubectl cluster-info
$ kubectl get nodes
$ kubectl apply -f k8s.yaml
$ kubectl port-forward svc/redis 6379:6379
$ kubectl port-forward svc/redis 8001:8001
```

8. Valkey vs Redis

- https://valkey.io/
- https://logz.io/blog/redis-no-longer-open-source-is-valkey-successor/
- https://opensourcewatch.beehiiv.com/p/valkey-8-sets-a-new-bar-for-open-source-in-memory-nosql-data-storage

```bash
$ k3d cluster create k8s-cluster
$ kubectl cluster-info
$ kubectl get nodes
$ kubectl apply -f k8s-valkey.yaml
$ kubectl port-forward svc/valkey 6379:6379
```

9. Valkey Cluster with Bitnami

- https://www.techtarget.com/searchitoperations/tutorial/Deploy-a-Redis-Cluster-on-Kubernetes
- https://hub.docker.com/r/bitnami/valkey-cluster
- https://hub.docker.com/r/bitnami/redis-cluster

- https://github.com/bitnami/charts/tree/main/bitnami/valkey-cluster
- https://www.techtarget.com/searchitoperations/tutorial/Deploy-a-Redis-Cluster-on-Kubernetes

-- Install with Helm

```bash
$ helm install my-release --set password=secret oci://registry-1.docker.io/bitnamicharts/valkey-cluster
$ helm list
NAME      	NAMESPACE	REVISION	UPDATED                              	STATUS  	CHART               	APP VERSION
my-release	default  	1       	2024-09-30 14:46:47.146021 +0200 CEST	deployed	valkey-cluster-1.0.1	8.0.0
# get the password
$ export VALKEY_PASSWORD=$(kubectl get secret --namespace "default" my-release-valkey-cluster -o jsonpath="{.data.valkey-password}" | base64 -d)
$ kubectl get all
NAME                                   READY   STATUS    RESTARTS   AGE
pod/my-release-valkey-cluster-4        1/1     Running   0          4m9s
pod/my-release-valkey-cluster-0        1/1     Running   0          4m9s
pod/my-release-valkey-cluster-1        1/1     Running   0          4m9s
pod/my-release-valkey-cluster-3        1/1     Running   0          4m9s
pod/my-release-valkey-cluster-2        1/1     Running   0          4m9s
pod/my-release-valkey-cluster-5        1/1     Running   0          4m9s
pod/my-release-valkey-cluster-client   1/1     Running   0          2m56s

NAME                                         TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)              AGE
service/kubernetes                           ClusterIP   10.43.0.1      <none>        443/TCP              87m
service/my-release-valkey-cluster            ClusterIP   10.43.134.53   <none>        6379/TCP             4m9s
service/my-release-valkey-cluster-headless   ClusterIP   None           <none>        6379/TCP,16379/TCP   4m9s

NAME                                         READY   AGE
statefulset.apps/my-release-valkey-cluster   6/6     4m10s
```

-- Run a `Valkey` pod that you can use as a client:

```bash
kubectl run --namespace default my-release-valkey-cluster-client --rm --tty -i --restart='Never' \
 --env VALKEY_PASSWORD=secret \
--image docker.io/bitnami/valkey-cluster:8.0.0-debian-12-r0 -- bash
```

-- Connect using the Valkey CLI: (`-c` means enable cluster mode)

```bash 
$ valkey-cli -c -h my-release-valkey-cluster -a secret
$ my-release-valkey-cluster:6379> cluster info
cluster_state:ok
cluster_slots_assigned:16384
cluster_slots_ok:16384
cluster_slots_pfail:0
cluster_slots_fail:0
cluster_known_nodes:6
cluster_size:3
cluster_current_epoch:6
cluster_my_epoch:1
cluster_stats_messages_ping_sent:383
cluster_stats_messages_pong_sent:383
cluster_stats_messages_sent:766
cluster_stats_messages_ping_received:378
cluster_stats_messages_pong_received:386
cluster_stats_messages_meet_received:5
cluster_stats_messages_received:769
total_cluster_links_buffer_limit_exceeded:0

$ my-release-valkey-cluster:6379> acl list
1) "user default on sanitize-payload #95d30169a59c418b52013315fc81bc99fdf0a7b03a116f346ab628496f349ed5 ~* &* +@all"

$ $ my-release-valkey-cluster:6379> cluster nodes
fa0edaccf7dfad4d74c159e5179bbab5badc7363 10.42.0.24:6379@16379 myself,master - 0 0 2 connected 5461-10922
c7a36c1768fd9552e7a8c3f9e809e923c63d9c48 10.42.0.27:6379@16379 slave 1e7ff9ae148daaab1735d1ab9f5f62d1fa4adb5a 0 1727705107000 1 connected
1e7ff9ae148daaab1735d1ab9f5f62d1fa4adb5a 10.42.0.29:6379@16379 master - 0 1727705108018 1 connected 0-5460
41e60c17d7d461681b20079b508c1909c56454ea 10.42.0.28:6379@16379 slave ab55786d6781144a7f6cefb1414983c0376bf8d5 0 1727705105997 3 connected
ab55786d6781144a7f6cefb1414983c0376bf8d5 10.42.0.26:6379@16379 master - 0 1727705105000 3 connected 10923-16383
760d099fe7c46a0f57f3a367d694aa20da3f99d5 10.42.0.25:6379@16379 slave fa0edaccf7dfad4d74c159e5179bbab5badc7363 0 1727705107008 2 connected

$  my-release-valkey-cluster:6379> info keyspace
# Keyspace
db0:keys=14,expires=14,avg_ttl=28797
```

### Redis UI :

- https://www.dragonflydb.io/guides/redis-gui

RedisInsight: https://redis.io/insight/

### Redis Cluster vs Redis Sentinel

- `Redis Cluster`
    - is a distributed version of Redis that provides a way to run a Redis installation where data is automatically
      sharded across multiple Redis nodes.
    - Data is automatically divided across multiple nodes in a Redis cluster, enabling horizontal scaling and improving
      performance.
    - Like Redis Sentinel, Redis Cluster also provides high availability. It can survive partitions where a majority of
      master nodes are reachable and at least one reachable master node exists for every hash slot.
    - Redis Cluster does not use Sentinel nodes. Instead, all nodes are responsible for failover and configuration
      settings, which simplifies the system.
    - Is for use cases that need to automatically shard data over multiple nodes to achieve higher levels of
      scalability.

- `Redis Sentinel`
    - provides high availability for Redis with a system that automatically detects master node failures and replaces it
      with a slave node.
    - All write and read operations still go to a single master node, which could be a potential bottleneck in a
      large-scale system.
    - Is ideal for those who need a robust failover mechanism without the complexities of data sharding
    - It’s a great choice for applications where high availability is a priority, and all data can reside on a single
      Redis node.

### Error when running the app:

```bash
org.springframework.data.redis.RedisSystemException: Error in execution
Caused by: io.lettuce.core.RedisCommandExecutionException: MOVED 5763 10.42.0.39:6379
```

From: https://stackoverflow.com/questions/74029849/caused-by-io-lettuce-core-rediscommandexecutionexception-moved-15596-xx-x-xxx-x

Try using `RedisClusterClient`, instead of `RedisClient`. The unhandled MOVED response indicates that you are trying to
use the non-cluster-aware client with Redis deployed in cluster mode.

io.lettuce.core.cluster.RedisClusterClient
io.lettuce.core.RedisClient

org.springframework.data.redis.cache.RedisCacheManager


Running within the cluster with `k8s`  profile it works.

```bash
$ mvn clean package spring-boot:build-image
$ k3d images import spring-cache-redis:0.0.1-SNAPSHOT -c k8s-cluster 
$ docker exec k3d-k8s-cluster-server-0 crictl images | grep spring-cache-redis
docker.io/library/spring-cache-redis   0.0.1-SNAPSHOT         9dab214fd7203       361MB
$ kubectl apply -f k8s/k8s.yaml
$ kubectl apply -f k8s-redisinsight.yaml
$ kubectl port-forward svc/redisinsight-service 5540:5540
$ open http://localhost:5540
```

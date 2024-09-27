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


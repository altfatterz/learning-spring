### Spring Reactive Redis

- `Install Redis` - we need it only for `redis-cli`

```bash
$ brew update
$ brew install redis
$ redis-server --version
Redis server v=7.2.5 sha=00000000:0 malloc=libc bits=64 build=bd81cd1340e80580
$ redis-cli --version
redis-cli 7.2.5
```
More details: https://redis.io/docs/latest/operate/oss_and_stack/install/install-redis/install-redis-on-mac-os/

- `Start redis`

We use Redis-Stack: https://hub.docker.com/r/redis/redis-stack

```bash
$ docker compose up 
```

- `Connect to Redis`

```bash
$ redis-cli info modules // check the installed modules
$ redis-cli -h localhost -p 6379
$ keys * // Get the keys
$ dbsize // Check number of keys in database
$ set foo bar EX 60 // add a 'foo' key with value 'bar' with 60 second expiration
$ get foo
"bar" 
$ get foo // after 60 seconds
(nil)
$ exists foo //  check if key exists
$ delete foo // delete a key
$ expire foo 600 // set expiry to key
$ persist foo // remove expiry from key
$ ttl foo // find (remaining) time to live of a key
$ set nr 1
$ incr nr // increment a number
$ decr nr // decrement a number
$ json.set person $ '{"name":"Leonard Cohen","dob":1478476800,"isActive": true, "hobbies":["music", "cricket"]}'
$ json.get person
$ json.objlen person
(integer) 4
$ json.objkeys person
1) "name"
2) "dob"
3) "isActive"
4) "hobbies"
$ json.set person $.name '"Alex" // update nested property
$ json.set person $.hobbies[1] '"dance"' // update nested array
```

- Start the application `SpringReactiveRedisApplication`

- Check Redis content

The entries were create a string key

```bash
$ 127.0.0.1:6379> keys *
1) "06b6e408-2df4-43c6-bed4-9000895da0b8"
2) "b4e148b2-76ab-46da-8854-8b0c55f421e4"
3) "0e728011-a7b0-4633-b0fb-982afc3cbbac
$ 127.0.0.1:6379> get 06b6e408-2df4-43c6-bed4-9000895da0b8
"{\"id\":\"06b6e408-2df4-43c6-bed4-9000895da0b8\",\"name\":\"Black Alert Redis\"}"
```

### Redis Hashes

- Redis hashes are record types structured as collections of field-value pairs

```bash
> HSET bike:1 model Deimos brand Ergonom type 'Enduro bikes' price 4972
(integer) 4
> HGET bike:1 model
"Deimos"
> HGET bike:1 price
"4972"
> HGETALL bike:1
1) "model"
2) "Deimos"
3) "brand"
4) "Ergonom"
5) "type"
6) "Enduro bikes"
7) "price"
8) "4972"
```

### Resources:

Spring Date Reactive Redis: https://spring.io/guides/gs/spring-data-reactive-redis
Redis Getting Started: https://redis.io/learn/howtos/quick-start
Redis Cache: https://docs.spring.io/spring-data/redis/reference/redis/redis-cache.html
Redis Cluster: https://docs.spring.io/spring-data/redis/reference/redis/cluster.html
Caching: https://docs.spring.io/spring-boot/reference/io/caching.html

https://positivethinking.tech/insights/redis-cache-in-spring-boot-applications/


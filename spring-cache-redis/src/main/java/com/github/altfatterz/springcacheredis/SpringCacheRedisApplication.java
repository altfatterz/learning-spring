package com.github.altfatterz.springcacheredis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

// The @EnableCaching annotation triggers a post-processor that inspects every Spring bean for the presence
// of caching annotations on public methods. If such an annotation is found, a proxy is automatically created
// to intercept the method call and handle the caching behavior accordingly.

// The post-processor handles the @Cacheable, @CachePut and @CacheEvict annotations
// More details:  https://docs.spring.io/spring-framework/reference/integration/cache.html

// Spring Boot automatically configures a suitable CacheManager to serve as a provider for the relevant cache.

@SpringBootApplication
@EnableCaching
public class SpringCacheRedisApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCacheRedisApplication.class, args);
    }

}

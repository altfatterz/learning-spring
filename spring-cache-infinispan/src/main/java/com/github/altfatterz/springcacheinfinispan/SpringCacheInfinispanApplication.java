package com.github.altfatterz.springcacheinfinispan;

import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.event.EventListener;

@SpringBootApplication
@EnableCaching
public class SpringCacheInfinispanApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringCacheInfinispanApplication.class, args);
    }

}

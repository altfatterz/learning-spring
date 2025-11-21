package com.github.altfatterz.springcacheinfinispan;

import org.infinispan.spring.remote.provider.SpringRemoteCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AppRunner implements CommandLineRunner {

    @Autowired
    private SpringRemoteCacheManager remoteCacheManager;

    private static final Logger logger = LoggerFactory.getLogger(AppRunner.class);

    private final BookRepository bookRepository;

    private final Random random = new Random();

    public AppRunner(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // create the cache
        remoteCacheManager.getNativeCacheManager()
                .administration().getOrCreateCache("books", "distributed");

        logger.info(".... Fetching books");
//        for (int i = 0; i < 10; i++) {
//            int nr = random.nextInt(1000000);
//            String key = "isbn-" + nr;
//            logger.info(key + " -->" + bookRepository.getByIsbn(key));
//        }

        while (true) {
            int nr = random.nextInt(1000000);
            String key = "isbn-" + nr;
            logger.info(key + " -->" + bookRepository.getByIsbn(key));
        }
    }

}

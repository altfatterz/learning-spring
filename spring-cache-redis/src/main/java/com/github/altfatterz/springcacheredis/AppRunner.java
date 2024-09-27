package com.github.altfatterz.springcacheredis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class AppRunner implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(AppRunner.class);

    private final BookRepository bookRepository;

    private final Random random = new Random();

    public AppRunner(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        logger.info(".... Fetching books");
        while (true) {
            int nr = random.nextInt(100);
            String key = "isbn-" + nr;
            logger.info(key + " -->" + bookRepository.getByIsbn(key));
        }
    }

}

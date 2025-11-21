package com.github.altfatterz.springcacheinfinispan;

import com.github.javafaker.Faker;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class SimpleBookRepository implements BookRepository {

    private Faker faker = new Faker();


    @Override
    @Cacheable("books")
    public Book getByIsbn(String isbn) {
        simulateSlowService();
        String content = RandomStringUtils.randomAlphabetic(100000);
        return new Book(isbn, faker.book().title(), content);
    }

    // Don't do this at home
    private void simulateSlowService() {
        try {
            long time = 100L;
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

}
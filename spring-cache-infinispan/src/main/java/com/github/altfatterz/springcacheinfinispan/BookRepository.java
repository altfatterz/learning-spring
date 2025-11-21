package com.github.altfatterz.springcacheinfinispan;

public interface BookRepository {

    Book getByIsbn(String isbn);

}
package com.github.altfatterz.springcacheredis;

public interface BookRepository {

    Book getByIsbn(String isbn);

}
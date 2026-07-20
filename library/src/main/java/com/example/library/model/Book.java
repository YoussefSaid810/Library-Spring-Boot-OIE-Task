package com.example.library.model;


public record Book(
    Long id,
    String title,
    String author,
    String category,
    boolean available
) {}

package com.masterlab.catalogue_management.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Builder // <== this is required for Book.builder() to work
@Entity
@Getter
@Setter
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(hidden = true)
    private Long id;

    private String name;
    private String isbnNumber;
    private LocalDate publishDate;
    private Double price;

    @Enumerated(EnumType.STRING)
    private BookType bookType;

    public Book(){

    }
    public Book(Long id, String name, String isbnNumber, LocalDate now, double price, BookType ebook) {
        this.id = id;
        this.name = name;
        this.isbnNumber = isbnNumber;
        this.publishDate = now;
        this.price = price;
        this.bookType = ebook;
    }

}

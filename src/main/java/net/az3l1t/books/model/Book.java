package net.az3l1t.books.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "books")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vendor_code", nullable = false)
    private String vendorCode;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int year;

    @Column
    private String brand;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private double price;
}
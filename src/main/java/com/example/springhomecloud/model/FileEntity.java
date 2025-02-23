package com.example.springhomecloud.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;

@Data
@Entity
@Table(name = "files")
public class FileEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String contentType;

    private Long size;

    @Column(nullable = false)
    private LocalDateTime uploadDateTime;

    @Lob
    @Column(columnDefinition = "bytea", nullable = false)
    @JdbcTypeCode(Types.BINARY)
    private byte[] data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
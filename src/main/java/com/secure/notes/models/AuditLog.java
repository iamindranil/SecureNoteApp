package com.secure.notes.models;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String action;
    private String username;
    private Long noteId;
    @jakarta.persistence.Column(columnDefinition = "TEXT")
    private String noteContent;//With no @Column annotation, Hibernate maps String to VARCHAR(255) by default on MySQL
    private LocalDateTime timeStamp;
}

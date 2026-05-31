package com.veterinary.clinic.models;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @CreatedDate
    @Column(name = "data_utworzenia", nullable = false, updatable = false)
    private LocalDateTime dataUtworzenia;

    @LastModifiedDate
    @Column(name = "data_modyfikacji", nullable = false)
    private LocalDateTime dataModyfikacji;

    public LocalDateTime getDataUtworzenia() { return dataUtworzenia; }
    public LocalDateTime getDataModyfikacji() { return dataModyfikacji; }
}
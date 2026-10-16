package com.epam.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Set;

@Entity
@Table(name = "training_types")
public class TrainingType {

    public static final Set<String> FIXED_NAMES = Set.of(
            "FITNESS",
            "YOGA",
            "ZUMBA",
            "STRETCHING",
            "RESISTANCE");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "training_type_name", nullable = false, unique = true)
    private String name;

    protected TrainingType() {
        // Required by Hibernate.
    }

    public TrainingType(String name) {
        if (name == null || !FIXED_NAMES.contains(name)) {
            throw new IllegalArgumentException("Unknown training type: " + name);
        }
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}

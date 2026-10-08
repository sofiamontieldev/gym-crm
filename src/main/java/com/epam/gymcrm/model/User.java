package com.epam.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.Objects;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, length = 10)
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected User() {
        // Required by Hibernate.
    }

    private User(Builder builder) {
        this.id = builder.id;
        this.firstName = requireText(builder.firstName, "firstName");
        this.lastName = requireText(builder.lastName, "lastName");
        this.username = requireText(builder.username, "username");
        this.password = requirePassword(builder.password);
        this.active = builder.active;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    /**
     * Returns the generated password for authentication workflows.
     * The value is intentionally excluded from logs and toString().
     */
    public String getPassword() {
        return password;
    }

    public boolean isActive() {
        return active;
    }

    public void updatePersonalData(String firstName, String lastName) {
        this.firstName = requireText(firstName, "firstName");
        this.lastName = requireText(lastName, "lastName");
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }

    private static String requirePassword(String password) {
        String value = requireText(password, "password");
        if (value.length() != 10) {
            throw new IllegalArgumentException("password must contain exactly 10 characters");
        }
        return value;
    }

    public static class Builder {
        private Long id;
        private String firstName;
        private String lastName;
        private String username;
        private String password;
        private boolean active;

        public Builder setId(Long id) {
            this.id = id;
            return this;
        }

        public Builder setFirstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder setLastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder setUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder setPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder setActive(boolean active) {
            this.active = active;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof User user)) {
            return false;
        }
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", username='" + username + '\'' +
                ", active=" + active +
                '}';
    }
}

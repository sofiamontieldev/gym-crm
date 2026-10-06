package com.epam.gymcrm.model;

import java.time.LocalDate;

public class Trainee extends User{

    private LocalDate dateOfBirth;
    private String address;

    public Trainee(User.Builder userBuilder,
                   LocalDate dateOfBirth,
                   String address) {

        super(userBuilder);
        this.dateOfBirth = dateOfBirth;
        this.address = address;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }




}


package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDAO;
import org.springframework.stereotype.Service;

@Service
public class TrainerService {

    private final TrainerDAO trainerDAO;
    private final PasswordGenerator passwordGenerator;

    public TrainerService(
            TrainerDAO trainerDAO,
            PasswordGenerator passwordGenerator) {

        this.trainerDAO = trainerDAO;
        this.passwordGenerator = passwordGenerator;
    }
}

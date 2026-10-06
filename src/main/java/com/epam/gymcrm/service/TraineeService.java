package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TraineeDAO;
import org.springframework.stereotype.Service;

@Service
public class TraineeService {

    private final TraineeDAO traineeDAO;
    private final PasswordGenerator passwordGenerator;

    public TraineeService(TraineeDAO traineeDAO, PasswordGenerator passwordGenerator) {
        this.traineeDAO = traineeDAO;
        this.passwordGenerator = passwordGenerator;
    }

}

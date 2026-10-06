package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TraineeDAO {

    private Map<Long, Trainee> storage;

    public TraineeDAO(
        @Qualifier("traineeStorage")
        Map<Long, Trainee> storage) {

        this.storage = storage;
    }
}

package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TrainerDAO {

    private final Map<Long, Trainer> storage;

    public TrainerDAO(
            @Qualifier("trainerStorage")
            Map<Long, Trainer> storage) {
        this.storage = storage;
    }
}


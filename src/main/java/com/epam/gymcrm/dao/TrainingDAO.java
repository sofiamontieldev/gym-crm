package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Training;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
public class TrainingDAO {

    private final Map<Long, Training> storage;

    public TrainingDAO(
            @Qualifier("trainingStorage")
            Map<Long, Training> storage) {

        this.storage = storage;
    }
}

package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainee;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TraineeDAO {

    private final Map<Long, Trainee> storage;

    public TraineeDAO(
        @Qualifier("traineeStorage")
        Map<Long, Trainee> storage) {

        this.storage = storage;
    }

    public void save(Trainee trainee) {
        storage.put(trainee.getId(), trainee);
    }

    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Trainee> findAll() {
        return new ArrayList<>(storage.values());
    }

    public void deleteById(Long id) {
        storage.remove(id);
    }
}

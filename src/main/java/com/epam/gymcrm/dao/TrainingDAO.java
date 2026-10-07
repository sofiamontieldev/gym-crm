package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Training;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDAO {

    private final Map<Long, Training> storage;

    public TrainingDAO(
            @Qualifier("trainingStorage")
            Map<Long, Training> storage) {

        this.storage = storage;
    }

    public void save(Long id, Training training) {
        storage.put(id, training);
    }

    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Training> findAll() {
        return new ArrayList<>(storage.values());
    }

    public long nextId() {
        return storage.keySet()
                .stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L) + 1;
    }
}

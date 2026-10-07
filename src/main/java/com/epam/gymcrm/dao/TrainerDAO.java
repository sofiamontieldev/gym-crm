package com.epam.gymcrm.dao;

import com.epam.gymcrm.model.Trainer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDAO {

    private final Map<Long, Trainer> storage;

    public TrainerDAO(
            @Qualifier("trainerStorage")
            Map<Long, Trainer> storage) {
        this.storage = storage;
    }

    public void save(Trainer trainer) {
        storage.put(trainer.getId(), trainer);
    }

    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.get(id));
    }

    public List<Trainer> findAll() {
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


package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.TrainerDAO;
import com.epam.gymcrm.dao.UserDAO;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import com.epam.gymcrm.integration.reports.TrainerWorkloadEventEmitter;
import com.epam.gymcrm.model.User;
import com.epam.gymcrm.model.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;
    private final TrainerDAO trainerDAO;
    private final TrainerWorkloadEventEmitter workloadEventEmitter;

    public UserService(
            UserDAO userDAO,
            TrainerDAO trainerDAO,
            TrainerWorkloadEventEmitter workloadEventEmitter) {
        this.userDAO = userDAO;
        this.trainerDAO = trainerDAO;
        this.workloadEventEmitter = workloadEventEmitter;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userDAO.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    @Transactional
    public User changeStatus(String username, boolean active) {
        User user = findByUsername(username);
        if (active) {
            user.activate();
        } else {
            user.deactivate();
        }
        if (user.getRole() == UserRole.TRAINER) {
            workloadEventEmitter.trainerStatusChanged(
                    trainerDAO.findByUsername(username)
                            .orElseThrow(() -> new ResourceNotFoundException(
                                    "Trainer not found: " + username)));
        }
        log.info("User status changed for username {} to active={}", username, active);
        return user;
    }
}

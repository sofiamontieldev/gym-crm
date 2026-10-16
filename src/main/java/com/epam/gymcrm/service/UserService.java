package com.epam.gymcrm.service;

import com.epam.gymcrm.dao.UserDAO;
import com.epam.gymcrm.exception.ResourceNotFoundException;
import com.epam.gymcrm.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
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
        log.info("User status changed for username {} to active={}", username, active);
        return user;
    }
}

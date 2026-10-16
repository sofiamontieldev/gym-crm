package com.epam.gymcrm.security;

import com.epam.gymcrm.dao.UserDAO;
import com.epam.gymcrm.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GymUserDetailsService implements UserDetailsService {

    private final UserDAO userDAO;

    public GymUserDetailsService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) {
        User user = userDAO.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Invalid username or password"));
        return new GymPrincipal(
                user.getUsername(), user.getPasswordHash(), user.getRole(), user.isActive());
    }
}

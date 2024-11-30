package com.springboot.blog.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.springboot.blog.entity.Role;
import com.springboot.blog.entity.User;
import com.springboot.blog.repository.RoleRepository;
import com.springboot.blog.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private static final String ADMIN_ROLE_NAME = "ROLE_ADMIN";
    private static final String USER_ROLE_NAME = "ROLE_USER";

    /**
     * Promotes a user to the ADMIN role.
     *
     * @param adminUsername the username of the current admin making the request.
     * @param userId        the ID of the user to be promoted.
     */
    @Transactional
    public void promoteUserToAdmin(String adminUsername, Long userId) {
        // Validate the admin making the request
        User adminUser = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        // Ensure that the admin is actually an admin
        if (!isAdmin(adminUser)) {
            throw new IllegalArgumentException("Only admins can promote users to admin.");
        }

        // Fetch the target user
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Log the promotion attempt
        System.out.println("Promoting user " + targetUser.getUsername() + " to ADMIN by " + adminUser.getUsername());

        // Fetch the admin role
        Role adminRole = roleRepository.findByName(ADMIN_ROLE_NAME)
                .orElseThrow(() -> new IllegalStateException("Admin role not found."));

        // Add the admin role to the target user if not already present
        if (!targetUser.getRoles().contains(adminRole)) {
            targetUser.getRoles().clear();
            targetUser.getRoles().add(adminRole);
            userRepository.save(targetUser);

            // Log after role assignment
            System.out.println("User " + targetUser.getUsername() + " promoted to ADMIN.");
        } else {
            throw new IllegalStateException("User is already an ADMIN.");
        }
    }

    @Transactional
    public void demoteAdminToUser(String adminUsername, Long userId) {
        // Validate the admin making the request
        User adminUser = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        // Ensure that the admin making the request is actually an admin
        if (!isAdmin(adminUser)) {
            throw new IllegalArgumentException("Only admins can demote other admins.");
        }

        // Fetch the target user
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        // Log the demotion attempt
        System.out.println("Demoting admin " + targetUser.getUsername() + " to USER by " + adminUser.getUsername());

        // Fetch the user role
        Role userRole = roleRepository.findByName(USER_ROLE_NAME)
                .orElseThrow(() -> new IllegalStateException("User role not found."));

        // Ensure the target user is an admin before demotion
        Role adminRole = roleRepository.findByName(ADMIN_ROLE_NAME)
                .orElseThrow(() -> new IllegalStateException("Admin role not found."));

        if (targetUser.getRoles().contains(adminRole)) {
            // Remove the admin role and assign the user role
            targetUser.getRoles().clear();
            targetUser.getRoles().add(userRole);
            userRepository.save(targetUser);

            // Log after role reassignment
            System.out.println("Admin " + targetUser.getUsername() + " demoted to USER.");
        } else {
            throw new IllegalStateException("Target user is not an ADMIN.");
        }
    }

    /**
     * Lists all users except those with the ADMIN role.
     *
     * @return List of non-admin users.
     */
    public List<User> listNonAdminUsers() {
        Role adminRole = roleRepository.findByName(ADMIN_ROLE_NAME)
                .orElseThrow(() -> new IllegalArgumentException("Admin role not found."));

        // Fetch all users and filter out the ones who have the admin role
        return userRepository.findAll().stream()
                .filter(user -> !user.getRoles().contains(adminRole))
                .collect(Collectors.toList());
    }

    public List<User> listAllAdmins() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalArgumentException("Admin role not found."));

        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().contains(adminRole))
                .toList();
    }

    /**
     * Checks if a user is an admin.
     *
     * @param user the user to check.
     * @return true if the user has the ADMIN role, false otherwise.
     */
    private boolean isAdmin(User user) {
        // Check if the user has the ROLE_ADMIN role
        return user.getRoles().stream()
                .anyMatch(role -> ADMIN_ROLE_NAME.equals(role.getName()));
    }
}

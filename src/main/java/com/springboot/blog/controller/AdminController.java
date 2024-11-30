package com.springboot.blog.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.springboot.blog.entity.User;
import com.springboot.blog.service.AdminService;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "AdminService")
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * Promote a user to ADMIN role.
     *
     * @param adminUsername the username of the current admin.
     * @param userId        the ID of the user to promote.
     * @return ResponseEntity indicating success or failure.
     */
    @PatchMapping("/promote/{userId}")
    public ResponseEntity<String> promoteUserToAdmin(
            @RequestHeader("adminUsername") String adminUsername,
            @PathVariable Long userId) {
        try {
            adminService.promoteUserToAdmin(adminUsername, userId);
            return ResponseEntity.ok("User promoted to ADMIN successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @PatchMapping("/demote/{adminId}")
    public ResponseEntity<String> demoteAdminToUser(
            @RequestHeader("adminUsername") String adminUsername,
            @PathVariable Long adminId) {
        try {
            adminService.demoteAdminToUser(adminUsername, adminId);
            return ResponseEntity.ok("Admin demoted to USER successfully.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    /**
     * Get a list of all non-admin users.
     *
     * @return ResponseEntity containing the list of non-admin users.
     */
    @GetMapping("/users")
    public ResponseEntity<List<User>> listNonAdminUsers() {
        return ResponseEntity.ok(adminService.listNonAdminUsers());
    }

    @GetMapping("/admins")
    public ResponseEntity<List<User>> listAllAdmins() {
        return ResponseEntity.ok(adminService.listAllAdmins());
    }

}

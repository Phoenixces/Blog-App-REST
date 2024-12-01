package com.springboot.blog.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.PostConstruct;

@Configuration
public class SuperAdminInitializer {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final PasswordEncoder passwordEncoder;
    private final Dotenv dotenv;

    public SuperAdminInitializer(NamedParameterJdbcTemplate namedParameterJdbcTemplate, PasswordEncoder passwordEncoder) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.passwordEncoder = passwordEncoder;
        this.dotenv = Dotenv.configure().directory("src/main/resources").load();
    }

    @PostConstruct
    public void init() {
        String adminEmail = dotenv.get("admin.email");
        String adminName = dotenv.get("admin.name");
        String adminUsername = dotenv.get("admin.username");
        String adminPassword = dotenv.get("admin.password");

        String encodedPassword = passwordEncoder.encode(adminPassword);

        // Check if the admin user already exists
        String countQuery = "SELECT COUNT(*) FROM users WHERE username = :username";
        MapSqlParameterSource countParams = new MapSqlParameterSource("username", adminUsername);
        Integer count = namedParameterJdbcTemplate.queryForObject(countQuery, countParams, Integer.class);

        if (count == null || count == 0) {
            // Insert the admin user
            String insertUserQuery = "INSERT INTO users (email, name, username, password) VALUES (:email, :name, :username, :password)";
            MapSqlParameterSource insertUserParams = new MapSqlParameterSource()
                    .addValue("email", adminEmail)
                    .addValue("name", adminName)
                    .addValue("username", adminUsername)
                    .addValue("password", encodedPassword);
            namedParameterJdbcTemplate.update(insertUserQuery, insertUserParams);

            // Get the admin user ID
            String userIdQuery = "SELECT id FROM users WHERE username = :username";
            Long userId = namedParameterJdbcTemplate.queryForObject(userIdQuery, countParams, Long.class);

            // Get the admin role ID
            String roleIdQuery = "SELECT id FROM roles WHERE name = 'ROLE_ADMIN'";
            Long roleId = namedParameterJdbcTemplate.queryForObject(roleIdQuery, new MapSqlParameterSource(), Long.class);

            // Assign the admin role to the admin user
            String insertUserRoleQuery = "INSERT INTO users_roles (user_id, role_id) VALUES (:userId, :roleId)";
            MapSqlParameterSource insertUserRoleParams = new MapSqlParameterSource()
                    .addValue("userId", userId)
                    .addValue("roleId", roleId);
            namedParameterJdbcTemplate.update(insertUserRoleQuery, insertUserRoleParams);
        }
    }
}
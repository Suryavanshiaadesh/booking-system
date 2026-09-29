package com.freelance.booking.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates a default admin on startup. Change ADMIN_EMAIL / ADMIN_PASSWORD in production. */
@Component
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String email;
    private final String password;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder,
                       @Value("${app.admin.email}") String email,
                       @Value("${app.admin.password}") String password) {
        this.users = users;
        this.encoder = encoder;
        this.email = email.toLowerCase();
        this.password = password;
    }

    @Override
    public void run(String... args) {
        if (!users.existsByEmail(email)) {
            users.save(new User("Admin", email, encoder.encode(password), Role.ADMIN));
        }
    }
}

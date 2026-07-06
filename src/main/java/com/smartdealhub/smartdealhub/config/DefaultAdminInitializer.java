package com.smartdealhub.smartdealhub.config;

import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class DefaultAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        long approvedAdmins = userRepository.countByRoleAndApprovalStatus(User.Role.ADMIN, User.ApprovalStatus.APPROVED);
        if (approvedAdmins > 0) {
            return;
        }
        if (userRepository.existsByEmail("admin@smartdealhub.com")) {
            return;
        }

        User admin = new User();
        admin.setName("System Admin");
        admin.setEmail("admin@smartdealhub.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(User.Role.ADMIN);
        admin.setApprovalStatus(User.ApprovalStatus.APPROVED);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);
    }
}

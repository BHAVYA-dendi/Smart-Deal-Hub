package com.smartdealhub.smartdealhub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartdealhub.smartdealhub.dto.LoginRequest;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.UserActivityRepository;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserActivityRepository userActivityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void cleanUsers() {
        userActivityRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void adminRegistrationAcceptsPasswordInRequestBody() throws Exception {
        String body = """
                {"name":"New Admin","email":"newadmin@test.com","password":"Admin@456"}
                """;

        mockMvc.perform(post("/api/users/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("PENDING"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void defaultAdminCanLoginAfterStartup() throws Exception {
        User admin = new User();
        admin.setName("System Admin");
        admin.setEmail("admin@smartdealhub.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(User.Role.ADMIN);
        admin.setApprovalStatus(User.ApprovalStatus.APPROVED);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@smartdealhub.com");
        loginRequest.setPassword("Admin@123");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void pendingStoreOwnerCannotLoginUntilApproved() throws Exception {
        User owner = new User();
        owner.setName("Pending Owner");
        owner.setEmail("owner@test.com");
        owner.setPassword(passwordEncoder.encode("Owner@123"));
        owner.setRole(User.Role.STORE_OWNER);
        owner.setApprovalStatus(User.ApprovalStatus.PENDING);
        owner.setActive(true);
        owner.setCreatedAt(LocalDateTime.now());
        userRepository.save(owner);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("owner@test.com");
        loginRequest.setPassword("Owner@123");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanApprovePendingUser() throws Exception {
        User admin = new User();
        admin.setName("System Admin");
        admin.setEmail("admin@smartdealhub.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(User.Role.ADMIN);
        admin.setApprovalStatus(User.ApprovalStatus.APPROVED);
        admin.setActive(true);
        admin.setCreatedAt(LocalDateTime.now());
        userRepository.save(admin);

        User pendingOwner = new User();
        pendingOwner.setName("Pending Owner");
        pendingOwner.setEmail("owner@test.com");
        pendingOwner.setPassword(passwordEncoder.encode("Owner@123"));
        pendingOwner.setRole(User.Role.STORE_OWNER);
        pendingOwner.setApprovalStatus(User.ApprovalStatus.PENDING);
        pendingOwner.setActive(true);
        pendingOwner.setCreatedAt(LocalDateTime.now());
        pendingOwner = userRepository.save(pendingOwner);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("admin@smartdealhub.com");
        loginRequest.setPassword("Admin@123");

        String loginResponse = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String token = objectMapper.readTree(loginResponse).get("token").asText();

        mockMvc.perform(put("/api/users/" + pendingOwner.getUserId() + "/approve")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.approvalStatus").value("APPROVED"));
    }
}

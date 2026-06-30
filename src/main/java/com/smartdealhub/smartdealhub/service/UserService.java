package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // ========================= BASIC CRUD =========================

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    public User addUser(User user) {
        requireRawPassword(user.getPassword());
        if (!isEncodedPassword(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        if (user.getRole() == User.Role.STORE_OWNER || user.getRole() == User.Role.ADMIN) {
            user.setApprovalStatus(User.ApprovalStatus.PENDING);
        } else {
            user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        }
        return userRepository.save(user);
    }

    public User updateUser(Long userId, User updatedUser) {
        User existing = getUserById(userId);
        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPhone(updatedUser.getPhone());
        existing.setRole(updatedUser.getRole());
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        return userRepository.save(existing);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // ========================= AUTHENTICATION / ACCOUNT =========================

    public User registerUser(User user, User.Role role) {
        requireRawPassword(user.getPassword());
        user.setRole(role);
        if (role == User.Role.ADMIN || role == User.Role.STORE_OWNER) {
            user.setApprovalStatus(User.ApprovalStatus.PENDING);
        } else {
            user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    private void requireRawPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    private boolean isEncodedPassword(String password) {
        return password != null && (password.startsWith("$2a$") || password.startsWith("$2b$"));
    }

    public User login(String email, String password) {
        User user = getUserByEmail(email);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        if (user.getRole() != User.Role.USER && user.getApprovalStatus() != User.ApprovalStatus.APPROVED) {
            throw new RuntimeException("Your account is pending admin approval");
        }
        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }
        user.setLoggedIn(true); // ensure loggedIn boolean field exists in User model
        return userRepository.save(user);
    }

    public void logout(Long userId) {
        User user = getUserById(userId);
        user.setLoggedIn(false); // ensure loggedIn boolean field exists in User model
        userRepository.save(user);
    }

    // ========================= AUTHENTICATION / ACCOUNT =========================
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getUserById(userId);
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    // ========================= USER MANAGEMENT =========================

    public void updateUserRole(Long userId, User.Role newRole) {
        User user = getUserById(userId);
        user.setRole(newRole);
        userRepository.save(user);
    }

    public void setUserActiveStatus(Long userId, boolean isActive) {
        User user = getUserById(userId);
        user.setActive(isActive); // ensure active boolean field exists in User model
        userRepository.save(user);
    }

    public List<User> getPendingApprovals() {
        return userRepository.findByApprovalStatus(User.ApprovalStatus.PENDING);
    }

    public User approveUser(Long userId) {
        User user = getUserById(userId);
        user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        return userRepository.save(user);
    }

    // ========================= ANALYTICS / STORE =========================

    public Object getStoreAnalytics(Long userId) {
        // Placeholder for store owner analytics logic
        return null;
    }
}
package com.smartdealhub.smartdealhub.service;

import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

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
        return userRepository.save(user);
    }

    public User updateUser(Long userId, User updatedUser) {
        User existing = getUserById(userId);
        existing.setName(updatedUser.getName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPhone(updatedUser.getPhone());
        existing.setRole(updatedUser.getRole());
        existing.setPassword(updatedUser.getPassword());
        return userRepository.save(existing);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    // ========================= AUTHENTICATION / ACCOUNT =========================

    public User registerUser(User user, User.Role role) {
        user.setRole(role);
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = getUserByEmail(email);
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid credentials");
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
        if (!user.getPassword().equals(oldPassword)) {
            throw new RuntimeException("Old password is incorrect");
        }
        user.setPassword(newPassword);
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

    // ========================= ANALYTICS / STORE =========================

    public Object getStoreAnalytics(Long userId) {
        // Placeholder for store owner analytics logic
        return null;
    }
}
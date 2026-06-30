package com.smartdealhub.smartdealhub.controller;

import com.smartdealhub.smartdealhub.dto.ChangePasswordRequest;
import com.smartdealhub.smartdealhub.dto.LoginRequest;
import com.smartdealhub.smartdealhub.dto.StoreAnalyticsResponse;
import com.smartdealhub.smartdealhub.dto.StoreOwnerRegistrationRequest;
import com.smartdealhub.smartdealhub.model.Store;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.model.UserActivity;
import com.smartdealhub.smartdealhub.model.User.Role;
import com.smartdealhub.smartdealhub.repository.UserRepository;
import com.smartdealhub.smartdealhub.security.JwtUtil;
import com.smartdealhub.smartdealhub.service.ActivityService;
import com.smartdealhub.smartdealhub.service.NotificationService;
import com.smartdealhub.smartdealhub.service.StoreService;
import com.smartdealhub.smartdealhub.service.UserService;
import com.smartdealhub.smartdealhub.service.VisitedStoreService;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.misc.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins="*")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ActivityService activityService;
    private final VisitedStoreService visitedStoreService;
    private final NotificationService notificationService;
    private final StoreService storeService;
    @Autowired
    private UserRepository userRepository;

    private User requireCurrentUser(HttpServletRequest request) {
        Object cu = request.getAttribute("currentUser");
        if (cu instanceof User user) return user;
        throw new RuntimeException("Unauthenticated");
    }

    // ==============================
    // USER ACCOUNT MANAGEMENT
    // ==============================
    @PostConstruct
    public void init() {
        System.out.println("UserController initialized!");
    }

    // Resolve current user from request (JWT or request attribute)
    @GetMapping("/me")
    public ResponseEntity<Map<String,Object>> me(HttpServletRequest request){
        try{
            Object cu = request.getAttribute("currentUser");
            User user = null;
            if(cu instanceof User u){ user = u; }
            if(user == null){
                String auth = request.getHeader("Authorization");
                if(auth != null && auth.startsWith("Bearer ")){
                    String email = JwtUtil.getEmailFromToken(auth.substring(7));
                    if(email != null){ user = userRepository.findByEmail(email).orElse(null); }
                }
            }
            if(user == null){ return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); }
            Map<String,Object> out = new HashMap<>();
            out.put("userId", user.getUserId());
            out.put("email", user.getEmail());
            out.put("name", user.getName());
            out.put("role", user.getRole()!=null? user.getRole().name(): null);
            out.put("approvalStatus", user.getApprovalStatus() != null ? user.getApprovalStatus().name() : null);
            out.put("active", user.isActive());
            return ResponseEntity.ok(out);
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/test")
    public String test() {
        System.out.println("Controller reached!");
        return "Test OK";
    }

    @PostMapping("/register")
    public ResponseEntity<?> addUser(@RequestBody Map<String, Object> body) {
        try {
            if (body == null) return ResponseEntity.badRequest().body("Missing payload");

            // Extract user data
            String name = (String) body.get("name");
            String email = (String) body.get("email");
            String password = (String) body.get("password");
            String phone = (String) body.get("phone");
            String roleStr = body.get("role") != null ? String.valueOf(body.get("role")) : "USER";

            // Validate required fields
            if (email == null || password == null || name == null) {
                return ResponseEntity.badRequest().body("Name, email, and password are required");
            }
            if (password.isBlank()) {
                return ResponseEntity.badRequest().body("Password is required");
            }
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.badRequest().body("Email already registered");
            }

            // Parse role
            Role role;
            try { 
                role = Role.valueOf(roleStr.toUpperCase()); 
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Invalid role. Must be one of: " + 
                    String.join(", ", java.util.Arrays.stream(Role.values()).map(Enum::name).toList()));
            }

            if (role == Role.ADMIN) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Use /api/users/register/admin for admin signup");
            }
            if (role == Role.STORE_OWNER) {
                return ResponseEntity.badRequest()
                        .body("Use /api/users/register/storeowner for store owner registration");
            }

            // Create and save user
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(password);
            user.setPhone(phone);
            user.setRole(role);
            user.setApprovalStatus(User.ApprovalStatus.APPROVED);
            user.setCreatedAt(LocalDateTime.now());
            User savedUser = userService.addUser(user);

            // Prepare response
            Map<String, Object> response = new HashMap<>();
            response.put("user", savedUser);
            response.put("token", JwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name()));

            // Handle store owner registration
            if (role == Role.STORE_OWNER) {
                // Extract store data
                String storeName = (String) body.get("storeName");
                String storeTypeStr = (String) body.get("storeType");
                String city = (String) body.get("city");
                String state = (String) body.get("state");
                Double latitude = null;
                Double longitude = null;
                
                // Parse coordinates if provided
                try {
                    if (body.get("latitude") != null) 
                        latitude = Double.valueOf(body.get("latitude").toString());
                    if (body.get("longitude") != null) 
                        longitude = Double.valueOf(body.get("longitude").toString());
                } catch (NumberFormatException e) {
                    return ResponseEntity.badRequest().body("Invalid latitude/longitude format");
                }

                // Validate store data
                if (storeName == null || storeName.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("storeName is required for STORE_OWNER");
                }
                if (storeTypeStr == null || storeTypeStr.trim().isEmpty()) {
                    return ResponseEntity.badRequest().body("storeType is required for STORE_OWNER");
                }

                // Parse store type
                Store.StoreType storeType;
                try {
                    storeType = Store.StoreType.valueOf(storeTypeStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.badRequest().body("Invalid storeType. Must be ONLINE or OFFLINE");
                }

                // Create and save store
                Store store = new Store();
                store.setName(storeName);
                store.setStoreType(storeType);
                store.setOwner(savedUser);
                store.setCity(city);
                store.setState(state);
                store.setLatitude(latitude);
                store.setLongitude(longitude);
                store.setCreatedAt(LocalDateTime.now());
                
                Store savedStore = storeService.saveStore(store);
                response.put("store", savedStore);
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                               .body("Registration failed: " + e.getMessage());
        }
    }
    @PostMapping("/register/storeowner")
    public ResponseEntity<?> registerStoreOwner(@RequestBody StoreOwnerRegistrationRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        if (request.getName() == null || request.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Name is required");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }

        if (request.getStoreName() == null || request.getStoreName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("storeName is required");
        }
        if (request.getStoreType() == null || request.getStoreType().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("storeType is required (ONLINE or OFFLINE)");
        }

        // Create user with STORE_OWNER role
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setPhone(request.getPhone());
        user.setRole(Role.STORE_OWNER);
        user.setApprovalStatus(User.ApprovalStatus.PENDING);
        user.setCreatedAt(java.time.LocalDateTime.now());
        User savedUser = userService.addUser(user);

        // Create the store tied to this owner
        Store store = new Store();
        store.setName(request.getStoreName());
        try {
            store.setStoreType(Store.StoreType.valueOf(request.getStoreType().toUpperCase()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Invalid storeType. Use ONLINE or OFFLINE");
        }
        store.setCity(request.getCity());
        store.setState(request.getState());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        Store savedStore = storeService.createStore(store, savedUser.getUserId());

        return ResponseEntity.ok(java.util.Map.of(
                "user", savedUser,
                "store", savedStore,
                "message", "Store owner registration submitted. Await admin approval."
        ));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<?> registerAdmin(@RequestBody User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            return ResponseEntity.badRequest().body("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("Password is required");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            return ResponseEntity.badRequest().body("Name is required");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email already registered");
        }
        User saved = userService.registerUser(user, Role.ADMIN);
        Map<String, Object> out = new HashMap<>();
        out.put("userId", saved.getUserId());
        out.put("email", saved.getEmail());
        out.put("role", saved.getRole().name());
        out.put("approvalStatus", saved.getApprovalStatus().name());
        out.put("message", "Admin registration submitted. Await approval from an existing admin.");
        return ResponseEntity.ok(out);
    }

    @PostMapping("/login") public ResponseEntity<Map<String,Object>> login(@RequestBody LoginRequest loginRequest) {
        User user = userService.login(loginRequest.getEmail(), loginRequest.getPassword());
        String token = JwtUtil.generateToken(user.getEmail(), user.getRole().name());
        // Log user login activity
        activityService.recordActivity(loginRequest.getEmail(),
                UserActivity.ActivityType.LOGIN,
                null,
                null,
                "User logged in");

        boolean needStoreSetup = false;
        Long storeId = null;
        if (user.getRole() == Role.STORE_OWNER) {
            List<Store> stores = storeService.getStoresByOwner(user.getUserId());
            needStoreSetup = (stores == null || stores.isEmpty());
            if (!needStoreSetup) {
                storeId = stores.get(0).getStoreId();
            }
        }

        // Build response without Map.of because it does not allow null values
        Map<String, Object> resp = new HashMap<>();
        resp.put("token", token);
        resp.put("userId", user.getUserId());
        resp.put("email", user.getEmail());
        resp.put("name", user.getName());
        resp.put("role", user.getRole().name());
        resp.put("approvalStatus", user.getApprovalStatus().name());
        resp.put("needStoreSetup", needStoreSetup);
        if (storeId != null) {
            resp.put("storeId", storeId);
        }
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout/{userId}")
    public ResponseEntity<String> logout(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.logout(userId);
        User user = userService.getUserById(userId);
        // Log user logout activity
        activityService.recordActivity(user.getEmail(),
                UserActivity.ActivityType.LOGOUT,
                null,
                null,
                "User logged out");
        return ResponseEntity.ok("User logged out successfully");
    }

    @PutMapping("/change-password/{userId}")
    public ResponseEntity<String> changePassword(
            @PathVariable Long userId,
            @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {
        User current = requireCurrentUser(httpRequest);
        if (current.getRole() != Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.changePassword(userId, request.getPassword(), request.getNewPassword());
        return ResponseEntity.ok("Password changed successfully");
    }

    @PutMapping("/{userId}")
    public ResponseEntity<User> updateUserProfile(@PathVariable Long userId, @RequestBody User updatedUser, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        User existing = userService.getUserById(userId);
        if (current.getRole() != Role.ADMIN) {
            updatedUser.setRole(existing.getRole());
        }
        updatedUser.setPassword(null);
        return ResponseEntity.ok(userService.updateUser(userId, updatedUser));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.deleteUser(userId);
        return ResponseEntity.ok("User deleted successfully");
    }

    // ==============================
    // 🔹 USER ACTIVITY & VISITED STORES
    // ==============================

    @GetMapping("/{userId}/activity")
    public ResponseEntity<List<UserActivity>> getUserActivity(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(activityService.getActivitiesByUserId(userId));
    }

    @GetMapping("/{userId}/visited-stores")
    public ResponseEntity<List<Store>> getVisitedStores(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(visitedStoreService.getVisitedStoresByUser(userId));
    }

    @PostMapping("/{userId}/visit-store/{storeId}")
    public ResponseEntity<String> addVisitedStore(@PathVariable Long userId, @PathVariable Long storeId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        visitedStoreService.addVisit(userId, storeId);
        // Log visit activity
        activityService.recordActivity(userId,
                UserActivity.ActivityType.VIEW_STORE,
                storeId,
                "STORE",
                "User visited store");
        return ResponseEntity.ok("Store visit recorded");
    }

    // ==============================
    // 🔹 ADMIN-SPECIFIC FEATURES
    // ==============================

    @GetMapping
    public ResponseEntity<List<Map<String,Object>>> getAllUsers(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<User> list = userService.getAllUsers();
        List<Map<String,Object>> summaries = list.stream().map(u -> {
            Map<String,Object> m = new HashMap<>();
            m.put("userId", u.getUserId());
            m.put("email", u.getEmail());
            m.put("name", u.getName());
            m.put("role", u.getRole()!=null? u.getRole().name(): null);
            m.put("loggedIn", u.isLoggedIn());
            m.put("active", u.isActive());
            m.put("approvalStatus", u.getApprovalStatus() != null ? u.getApprovalStatus().name() : null);
            return m;
        }).toList();
        return ResponseEntity.ok(summaries);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingUsers(HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Map<String, Object>> out = userService.getPendingApprovals().stream().map(u -> {
            Map<String, Object> row = new HashMap<>();
            row.put("userId", u.getUserId());
            row.put("name", u.getName());
            row.put("email", u.getEmail());
            row.put("role", u.getRole().name());
            row.put("approvalStatus", u.getApprovalStatus().name());
            return row;
        }).toList();
        return ResponseEntity.ok(out);
    }

    @PutMapping("/{userId}/approve")
    public ResponseEntity<Map<String, Object>> approveUser(@PathVariable Long userId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        User approved = userService.approveUser(userId);
        Map<String, Object> out = new HashMap<>();
        out.put("userId", approved.getUserId());
        out.put("role", approved.getRole().name());
        out.put("approvalStatus", approved.getApprovalStatus().name());
        out.put("message", "User approved successfully");
        return ResponseEntity.ok(out);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null || (currentUser.getRole() != Role.ADMIN && !currentUser.getUserId().equals(userId))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getUserById(userId));
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<String> updateUserRole(@PathVariable Long userId, @RequestBody Role role, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.updateUserRole(userId, Role.valueOf(role.name()));
        return ResponseEntity.ok("User role updated successfully");
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<String> toggleUserStatus(@PathVariable Long userId, @RequestParam boolean active, HttpServletRequest request) {
        User currentUser = (User) request.getAttribute("currentUser");
        if (currentUser == null || currentUser.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        userService.setUserActiveStatus(userId, active);
        return ResponseEntity.ok(active ? "User activated" : "User deactivated");
    }

    // ==============================
    // 🔹 STORE OWNER DASHBOARD (Optional)
    // ==============================

    @GetMapping("/{userId}/store-visitors")
    public ResponseEntity<List<User>> getStoreVisitors(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(visitedStoreService.getVisitorsForOwnerStores(userId));
    }

    @GetMapping("/{userId}/store-analytics")
    public ResponseEntity<StoreAnalyticsResponse> getStoreAnalytics(@PathVariable Long userId, HttpServletRequest request) {
        User current = requireCurrentUser(request);
        if (current.getRole() != Role.ADMIN && !current.getUserId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok((StoreAnalyticsResponse) userService.getStoreAnalytics(userId));
    }
}
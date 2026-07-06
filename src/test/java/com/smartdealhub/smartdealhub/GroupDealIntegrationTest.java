package com.smartdealhub.smartdealhub;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartdealhub.smartdealhub.dto.LoginRequest;
import com.smartdealhub.smartdealhub.model.Product;
import com.smartdealhub.smartdealhub.model.Store;
import com.smartdealhub.smartdealhub.model.User;
import com.smartdealhub.smartdealhub.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class GroupDealIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private UserRepository userRepository;
    @Autowired private UserActivityRepository userActivityRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private GroupDealRepository groupDealRepository;
    @Autowired private GroupMemberRepository groupMemberRepository;
    @Autowired private DealInviteRepository dealInviteRepository;

    private User creator;
    private User joiner;
    private Product product;

    @BeforeEach
    void setUp() {
        dealInviteRepository.deleteAll();
        groupMemberRepository.deleteAll();
        groupDealRepository.deleteAll();
        productRepository.deleteAll();
        storeRepository.deleteAll();
        userActivityRepository.deleteAll();
        userRepository.deleteAll();

        creator = saveUser("Creator", "creator@test.com", User.Role.USER);
        joiner = saveUser("Joiner", "joiner@test.com", User.Role.USER);

        Store store = new Store();
        store.setOwner(creator);
        store.setName("Test Store");
        store.setStoreType(Store.StoreType.OFFLINE);
        store.setCity("Test City");
        store.setState("TS");
        store.setCreatedAt(LocalDateTime.now());
        store = storeRepository.save(store);

        product = new Product();
        product.setStore(store);
        product.setName("Group Deal Product");
        product.setDescription("Test product");
        product.setPrice(new BigDecimal("99.99"));
        product.setActive(true);
        product.setGroupDealAllowed(true);
        product.setDealMaxMembers(5);
        product.setDealTitle("Bulk Buy");
        product.setCreatedAt(LocalDateTime.now());
        product = productRepository.save(product);
    }

    private User saveUser(String name, String email, User.Role role) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("Password@123"));
        user.setRole(role);
        user.setApprovalStatus(User.ApprovalStatus.APPROVED);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    private String loginToken(String email) throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail(email);
        req.setPassword("Password@123");
        String body = mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    @Test
    void createJoinApproveLeaveAndMineWorkflow() throws Exception {
        String creatorToken = loginToken(creator.getEmail());

        String createBody = """
                {"productId": %d}
                """.formatted(product.getId());
        String createResp = mockMvc.perform(post("/api/group-deals/create")
                        .header("Authorization", "Bearer " + creatorToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long dealId = objectMapper.readTree(createResp).get("id").asLong();

        String joinerToken = loginToken(joiner.getEmail());
        mockMvc.perform(post("/api/group-deals/" + dealId + "/join")
                        .param("userId", String.valueOf(joiner.getUserId()))
                        .header("Authorization", "Bearer " + joinerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(post("/api/group-deals/" + dealId + "/join")
                        .param("userId", String.valueOf(joiner.getUserId()))
                        .header("Authorization", "Bearer " + joinerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/group-deals/" + dealId + "/approve/" + joiner.getUserId())
                        .header("Authorization", "Bearer " + creatorToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/group-deals/mine")
                        .header("Authorization", "Bearer " + joinerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(dealId));

        mockMvc.perform(get("/api/group-deals/joinable/me")
                        .header("Authorization", "Bearer " + joinerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/group-deals/" + dealId + "/leave")
                        .param("userId", String.valueOf(joiner.getUserId()))
                        .header("Authorization", "Bearer " + joinerToken))
                .andExpect(status().isOk());
    }
}

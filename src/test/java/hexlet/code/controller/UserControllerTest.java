package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.AuthRequest;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.model.User;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void testGetAllUsers() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordDigest(passwordEncoder.encode("password"));
        user.setFirstName("John");
        user.setLastName("Doe");
        userRepository.save(user);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].lastName").value("Doe"));
    }

    @Test
    @WithMockUser
    void testGetUserById() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordDigest(passwordEncoder.encode("password"));
        user.setFirstName("John");
        user.setLastName("Doe");
        User savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/users/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @WithMockUser
    void testCreateUser() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("newuser@example.com");
        userCreateDTO.setPassword("password123");
        userCreateDTO.setFirstName("Jane");
        userCreateDTO.setLastName("Smith");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.lastName").value("Smith"));

        var createdUser = userRepository.findByEmail("newuser@example.com");
        assertThat(createdUser).isPresent();
        assertThat(createdUser.get().getFirstName()).isEqualTo("Jane");
        assertThat(createdUser.get().getLastName()).isEqualTo("Smith");
        assertThat(passwordEncoder.matches("password123", createdUser.get().getPasswordDigest())).isTrue();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateUser() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordDigest(passwordEncoder.encode("password"));
        user.setFirstName("John");
        user.setLastName("Doe");
        User savedUser = userRepository.save(user);

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setFirstName(JsonNullable.of("UpdatedName"));
        userUpdateDTO.setEmail(JsonNullable.of("updated@example.com"));

        mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedName"))
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        var updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo("UpdatedName");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testUpdateUserPassword() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordDigest(passwordEncoder.encode("oldpassword"));
        user.setFirstName("John");
        user.setLastName("Doe");
        User savedUser = userRepository.save(user);

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setPassword(JsonNullable.of("newpassword123"));

        mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk());

        var updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(passwordEncoder.matches("newpassword123", updatedUser.getPasswordDigest())).isTrue();
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testDeleteUser() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordDigest(passwordEncoder.encode("password"));
        user.setFirstName("John");
        user.setLastName("Doe");
        User savedUser = userRepository.save(user);

        mockMvc.perform(delete("/api/users/" + savedUser.getId()))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(savedUser.getId())).isEmpty();
    }

    @Test
    @WithMockUser
    void testCreateUserWithInvalidData() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("invalid-email");
        userCreateDTO.setPassword("12");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testCreateUserWithDuplicateEmail() throws Exception {
        User existingUser = new User();
        existingUser.setEmail("duplicate@example.com");
        existingUser.setPasswordDigest(passwordEncoder.encode("password"));
        existingUser.setFirstName("Existing");
        existingUser.setLastName("User");
        userRepository.save(existingUser);

        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("duplicate@example.com");
        userCreateDTO.setPassword("password123");
        userCreateDTO.setFirstName("Jane");
        userCreateDTO.setLastName("Smith");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void testCreateUserWithNullPassword() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("test@example.com");
        userCreateDTO.setPassword(null);
        userCreateDTO.setFirstName("John");
        userCreateDTO.setLastName("Doe");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testGetNonExistentUser() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com")
    void testUpdateNonExistentUser() throws Exception {
        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setFirstName(JsonNullable.of("UpdatedName"));

        mockMvc.perform(put("/api/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "nonexistent@example.com")
    void testDeleteNonExistentUser() throws Exception {
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    void testPartialUpdateUser() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordDigest(passwordEncoder.encode("password"));
        user.setFirstName("John");
        user.setLastName("Doe");
        User savedUser = userRepository.save(user);

        UserUpdateDTO userUpdateDTO = new UserUpdateDTO();
        userUpdateDTO.setFirstName(JsonNullable.of("UpdatedFirstName"));

        mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("UpdatedFirstName"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("test@example.com"));

        var updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();
        assertThat(updatedUser.getFirstName()).isEqualTo("UpdatedFirstName");
        assertThat(updatedUser.getLastName()).isEqualTo("Doe");
        assertThat(updatedUser.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    @WithMockUser
    void testUserDTOContainsUpdatedAt() throws Exception {
        User testUser = new User();
        testUser.setEmail("test-timestamp@example.com");
        testUser.setPasswordDigest(passwordEncoder.encode("password"));
        testUser.setFirstName("Timestamp");
        testUser.setLastName("Test");
        User savedUser = userRepository.save(testUser);

        assertThat(savedUser.getCreatedAt()).isNotNull();
        assertThat(savedUser.getUpdatedAt()).isNotNull();

        mockMvc.perform(get("/api/users/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("test-timestamp@example.com"))
                .andExpect(jsonPath("$.firstName").value("Timestamp"))
                .andExpect(jsonPath("$.lastName").value("Test"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void testLoginWithInvalidCredentials() throws Exception {
        AuthRequest authRequest = new AuthRequest();
        authRequest.setUsername("wrong@example.com");
        authRequest.setPassword("wrong");

        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }
}

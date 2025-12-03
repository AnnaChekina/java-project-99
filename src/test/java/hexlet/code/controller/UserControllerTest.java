package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.AuthRequest;
import hexlet.code.dto.UserCreateDTO;
import hexlet.code.dto.UserDTO;
import hexlet.code.dto.UserUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private TaskStatus testStatus;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        userRepository.deleteAll();
        taskStatusRepository.deleteAll();

        testStatus = new TaskStatus();
        testStatus.setName("Test Status");
        testStatus.setSlug("test_status");
        taskStatusRepository.save(testStatus);
    }

    @Test
    @WithMockUser
    void testGetAllUsers() throws Exception {
        User user1 = new User();
        user1.setEmail("test1@example.com");
        user1.setPasswordDigest(passwordEncoder.encode("password"));
        user1.setFirstName("John");
        user1.setLastName("Doe");
        userRepository.save(user1);

        User user2 = new User();
        user2.setEmail("test2@example.com");
        user2.setPasswordDigest(passwordEncoder.encode("password"));
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        userRepository.save(user2);

        MvcResult result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        List<UserDTO> usersFromController = objectMapper.readValue(
                responseContent,
                new TypeReference<List<UserDTO>>() { }
        );

        List<User> usersFromDB = userRepository.findAll();

        assertThat(usersFromController).hasSize(2);
        assertThat(usersFromDB).hasSize(2);

        List<String> controllerEmails = usersFromController.stream()
                .map(UserDTO::getEmail)
                .sorted()
                .collect(Collectors.toList());

        List<String> dbEmails = usersFromDB.stream()
                .map(User::getEmail)
                .sorted()
                .collect(Collectors.toList());

        assertThat(controllerEmails).containsExactlyElementsOf(dbEmails);
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

        MvcResult result = mockMvc.perform(get("/api/users/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UserDTO userFromController = objectMapper.readValue(responseContent, UserDTO.class);

        var userFromDB = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(userFromController.getEmail()).isEqualTo(userFromDB.getEmail());
        assertThat(userFromController.getFirstName()).isEqualTo(userFromDB.getFirstName());
        assertThat(userFromController.getLastName()).isEqualTo(userFromDB.getLastName());
    }

    @Test
    @WithMockUser
    void testCreateUser() throws Exception {
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("newuser@example.com");
        userCreateDTO.setPassword("password123");
        userCreateDTO.setFirstName("Jane");
        userCreateDTO.setLastName("Smith");

        MvcResult result = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UserDTO userFromController = objectMapper.readValue(responseContent, UserDTO.class);

        var userFromDB = userRepository.findByEmail("newuser@example.com").orElse(null);

        assertThat(userFromDB).isNotNull();
        assertThat(userFromController.getEmail()).isEqualTo(userFromDB.getEmail());
        assertThat(userFromController.getFirstName()).isEqualTo(userFromDB.getFirstName());
        assertThat(userFromController.getLastName()).isEqualTo(userFromDB.getLastName());
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

        MvcResult result = mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UserDTO userFromController = objectMapper.readValue(responseContent, UserDTO.class);

        var userFromDB = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(userFromController.getFirstName()).isEqualTo("UpdatedName");
        assertThat(userFromDB.getFirstName()).isEqualTo("UpdatedName");
        assertThat(userFromController.getEmail()).isEqualTo(userFromDB.getEmail());
        assertThat(userFromController.getLastName()).isEqualTo(userFromDB.getLastName());
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

        MvcResult result = mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UserDTO userFromController = objectMapper.readValue(responseContent, UserDTO.class);

        var userFromDB = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(userFromController.getFirstName()).isEqualTo("UpdatedFirstName");
        assertThat(userFromDB.getFirstName()).isEqualTo("UpdatedFirstName");
        assertThat(userFromController.getLastName()).isEqualTo("Doe");
        assertThat(userFromDB.getLastName()).isEqualTo("Doe");
        assertThat(userFromController.getEmail()).isEqualTo("test@example.com");
        assertThat(userFromDB.getEmail()).isEqualTo("test@example.com");
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

        MvcResult result = mockMvc.perform(put("/api/users/" + savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userUpdateDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UserDTO userFromController = objectMapper.readValue(responseContent, UserDTO.class);

        var updatedUser = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(userFromController.getEmail()).isEqualTo(updatedUser.getEmail());
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
    @WithMockUser(username = "test@example.com")
    void testDeleteUserWithAssignedTasks() throws Exception {
        User user = new User();
        user.setEmail("test@example.com");
        user.setPasswordDigest(passwordEncoder.encode("password"));
        user.setFirstName("John");
        user.setLastName("Doe");
        User savedUser = userRepository.save(user);

        Task task = new Task();
        task.setTitle("Assigned Task");
        task.setAssignee(savedUser);
        task.setTaskStatus(testStatus);
        task.setCreatedAt(LocalDate.now());
        taskRepository.save(task);

        mockMvc.perform(delete("/api/users/" + savedUser.getId()))
                .andExpect(status().isConflict());
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

        MvcResult result = mockMvc.perform(get("/api/users/" + savedUser.getId()))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        UserDTO userFromController = objectMapper.readValue(responseContent, UserDTO.class);

        var userFromDB = userRepository.findById(savedUser.getId()).orElseThrow();

        assertThat(userFromController.getEmail()).isEqualTo(userFromDB.getEmail());
        assertThat(userFromController.getFirstName()).isEqualTo(userFromDB.getFirstName());
        assertThat(userFromController.getLastName()).isEqualTo(userFromDB.getLastName());
        assertThat(userFromController.getCreatedAt()).isNotNull();
        assertThat(userFromController.getUpdatedAt()).isNotNull();
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

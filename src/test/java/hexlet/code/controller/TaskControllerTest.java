package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
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

import java.time.LocalDate;
import java.util.List;

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
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Label bugLabel;
    private Label featureLabel;
    private TaskStatus testStatus;
    private TaskStatus completedStatus;
    private User testUser;
    private User anotherUser;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
        userRepository.deleteAll();
        labelRepository.deleteAll();

        // Создаем статусы задач
        testStatus = new TaskStatus();
        testStatus.setName("In Progress");
        testStatus.setSlug("in_progress");
        testStatus.setCreatedAt(LocalDate.now());
        testStatus = taskStatusRepository.save(testStatus);

        completedStatus = new TaskStatus();
        completedStatus.setName("Completed");
        completedStatus.setSlug("completed");
        completedStatus.setCreatedAt(LocalDate.now());
        completedStatus = taskStatusRepository.save(completedStatus);

        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordDigest(passwordEncoder.encode("password"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setCreatedAt(LocalDate.now());
        testUser.setUpdatedAt(LocalDate.now());
        testUser = userRepository.save(testUser);

        anotherUser = new User();
        anotherUser.setEmail("another@example.com");
        anotherUser.setPasswordDigest(passwordEncoder.encode("password"));
        anotherUser.setFirstName("Jane");
        anotherUser.setLastName("Smith");
        anotherUser.setCreatedAt(LocalDate.now());
        anotherUser.setUpdatedAt(LocalDate.now());
        anotherUser = userRepository.save(anotherUser);

        bugLabel = new Label();
        bugLabel.setName("bug");
        bugLabel.setCreatedAt(LocalDate.now());
        bugLabel = labelRepository.save(bugLabel);

        featureLabel = new Label();
        featureLabel.setName("feature");
        featureLabel.setCreatedAt(LocalDate.now());
        featureLabel = labelRepository.save(featureLabel);
    }

    @Test
    @WithMockUser
    void testGetAllTasks() throws Exception {
        Task task1 = createTestTask("Task 1", 1, testStatus, testUser);
        Task task2 = createTestTask("Task 2", 2, testStatus, null);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].index").value(1))
                .andExpect(jsonPath("$[0].status").value("in_progress"))
                .andExpect(jsonPath("$[0].assignee_id").value(testUser.getId()))
                .andExpect(jsonPath("$[1].title").value("Task 2"))
                .andExpect(jsonPath("$[1].assignee_id").doesNotExist());
    }

    @Test
    @WithMockUser
    void testGetTaskById() throws Exception {
        Task task = createTestTask("Test Task", 10, testStatus, testUser);

        mockMvc.perform(get("/api/tasks/" + task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"))
                .andExpect(jsonPath("$.index").value(10))
                .andExpect(jsonPath("$.content").value("Test Description"))
                .andExpect(jsonPath("$.status").value("in_progress"))
                .andExpect(jsonPath("$.assignee_id").value(testUser.getId()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser
    void testCreateTask() throws Exception {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("New Task");
        taskCreateDTO.setIndex(5);
        taskCreateDTO.setContent("New Description");
        taskCreateDTO.setStatus("in_progress");
        taskCreateDTO.setAssigneeId(testUser.getId());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.index").value(5))
                .andExpect(jsonPath("$.content").value("New Description"))
                .andExpect(jsonPath("$.status").value("in_progress"))
                .andExpect(jsonPath("$.assignee_id").value(testUser.getId()));

        var createdTask = taskRepository.findAll().stream()
                .filter(t -> t.getTitle().equals("New Task"))
                .findFirst();
        assertThat(createdTask).isPresent();
        assertThat(createdTask.get().getIndex()).isEqualTo(5);
        assertThat(createdTask.get().getTaskStatus().getSlug()).isEqualTo("in_progress");
    }

    @Test
    @WithMockUser
    void testCreateTaskWithoutAssignee() throws Exception {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("Unassigned Task");
        taskCreateDTO.setStatus("in_progress");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Unassigned Task"))
                .andExpect(jsonPath("$.assignee_id").doesNotExist());
    }

    @Test
    @WithMockUser
    void testUpdateTask() throws Exception {
        Task task = createTestTask("Old Task", 1, testStatus, testUser);

        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setTitle(JsonNullable.of("Updated Task"));
        taskUpdateDTO.setIndex(JsonNullable.of(99));
        taskUpdateDTO.setContent(JsonNullable.of("Updated Description"));

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.index").value(99))
                .andExpect(jsonPath("$.content").value("Updated Description"))
                .andExpect(jsonPath("$.status").value("in_progress"))
                .andExpect(jsonPath("$.assignee_id").value(testUser.getId()));

        var updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Task");
        assertThat(updatedTask.getIndex()).isEqualTo(99);
    }

    @Test
    @WithMockUser
    void testPartialUpdateTask() throws Exception {
        Task task = createTestTask("Partial Update Task", 1, testStatus, testUser);

        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setTitle(JsonNullable.of("Partially Updated"));

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Partially Updated"))
                .andExpect(jsonPath("$.index").value(1))
                .andExpect(jsonPath("$.content").value("Test Description"));
    }

    @Test
    @WithMockUser
    void testUpdateTaskAssignee() throws Exception {
        Task task = createTestTask("Task with Assignee", 1, testStatus, testUser);

        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setAssigneeId(JsonNullable.of(anotherUser.getId()));

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.assignee_id").value(anotherUser.getId()));

        var updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getAssignee().getId()).isEqualTo(anotherUser.getId());
    }

    @Test
    @WithMockUser
    void testUpdateTaskStatus() throws Exception {
        Task task = createTestTask("Task for Status Update", 1, testStatus, testUser);

        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setStatus(JsonNullable.of("completed"));

        mockMvc.perform(put("/api/tasks/" + task.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("completed"));

        var updatedTask = taskRepository.findById(task.getId()).orElseThrow();
        assertThat(updatedTask.getTaskStatus().getId()).isEqualTo(completedStatus.getId());
    }

    @Test
    @WithMockUser
    void testDeleteTask() throws Exception {
        Task task = createTestTask("Task to Delete", 1, testStatus, null);

        mockMvc.perform(delete("/api/tasks/" + task.getId()))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    @WithMockUser
    void testDeleteTaskWithAssignee() throws Exception {
        Task task = createTestTask("Task with Assignee", 1, testStatus, testUser);

        mockMvc.perform(delete("/api/tasks/" + task.getId()))
                .andExpect(status().isNoContent());

        assertThat(taskRepository.findById(task.getId())).isEmpty();
    }

    @Test
    @WithMockUser
    void testCreateTaskWithInvalidData() throws Exception {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("");
        taskCreateDTO.setStatus("in_progress");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testCreateTaskWithoutStatus() throws Exception {
        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("Task without status");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testGetNonExistentTask() throws Exception {
        mockMvc.perform(get("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateNonExistentTask() throws Exception {
        TaskUpdateDTO taskUpdateDTO = new TaskUpdateDTO();
        taskUpdateDTO.setTitle(JsonNullable.of("Updated Name"));

        mockMvc.perform(put("/api/tasks/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteNonExistentTask() throws Exception {
        mockMvc.perform(delete("/api/tasks/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAccessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isUnauthorized());

        TaskCreateDTO taskCreateDTO = new TaskCreateDTO();
        taskCreateDTO.setTitle("Test Task");
        taskCreateDTO.setStatus("in_progress");

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskCreateDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void testTaskDTOContainsAllRequiredFields() throws Exception {
        Task task = createTestTask("Complete Task", 42, testStatus, testUser);

        mockMvc.perform(get("/api/tasks/" + task.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Complete Task"))
                .andExpect(jsonPath("$.index").value(42))
                .andExpect(jsonPath("$.content").value("Test Description"))
                .andExpect(jsonPath("$.status").value("in_progress"))
                .andExpect(jsonPath("$.assignee_id").value(testUser.getId()))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser
    void testFilterTasksByTitle() throws Exception {
        Task task1 = createTestTask("Create new feature", 1, testStatus, testUser);
        Task task2 = createTestTask("Fix old bug", 2, testStatus, testUser);

        mockMvc.perform(get("/api/tasks?titleCont=feature"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Create new feature"));
    }

    @Test
    @WithMockUser
    void testFilterTasksByAssignee() throws Exception {
        Task task1 = createTestTask("Task for user 1", 1, testStatus, testUser);
        Task task2 = createTestTask("Task for user 2", 2, testStatus, anotherUser);

        mockMvc.perform(get("/api/tasks?assigneeId=" + testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Task for user 1"))
                .andExpect(jsonPath("$[0].assignee_id").value(testUser.getId()));
    }

    @Test
    @WithMockUser
    void testFilterTasksByStatus() throws Exception {
        Task task1 = createTestTask("In Progress Task", 1, testStatus, testUser);
        Task task2 = createTestTask("Completed Task", 2, completedStatus, testUser);

        mockMvc.perform(get("/api/tasks?status=completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Completed Task"))
                .andExpect(jsonPath("$[0].status").value("completed"));
    }

    @Test
    @WithMockUser
    void testFilterTasksByLabel() throws Exception {
        Task task1 = createTestTask("Bug Task", 1, testStatus, testUser);
        task1.setLabels(List.of(bugLabel));
        taskRepository.save(task1);

        Task task2 = createTestTask("Feature Task", 2, testStatus, testUser);
        task2.setLabels(List.of(featureLabel));
        taskRepository.save(task2);

        mockMvc.perform(get("/api/tasks?labelId=" + bugLabel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Bug Task"));
    }

    @Test
    @WithMockUser
    void testFilterTasksByMultipleCriteria() throws Exception {
        Task task1 = createTestTask("Fix critical bug", 1, testStatus, testUser);
        task1.setLabels(List.of(bugLabel));
        taskRepository.save(task1);

        mockMvc.perform(get("/api/tasks?titleCont=critical&assigneeId=" + testUser.getId()
                        + "&status=in_progress&labelId=" + bugLabel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Fix critical bug"));
    }

    @Test
    @WithMockUser
    void testFilterTasksNoResults() throws Exception {
        createTestTask("Test Task", 1, testStatus, testUser);

        mockMvc.perform(get("/api/tasks?titleCont=nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    private Task createTestTask(String title, Integer index, TaskStatus status, User assignee) {
        Task task = new Task();
        task.setTitle(title);
        task.setIndex(index);
        task.setContent("Test Description");
        task.setTaskStatus(status);
        task.setAssignee(assignee);
        task.setCreatedAt(LocalDate.now());
        return taskRepository.save(task);
    }
}

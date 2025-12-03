package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

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
class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskStatusRepository.deleteAll();
    }

    @Test
    @WithMockUser
    void testGetAllTaskStatuses() throws Exception {
        TaskStatus status1 = new TaskStatus();
        status1.setName("Draft");
        status1.setSlug("draft");
        taskStatusRepository.save(status1);

        TaskStatus status2 = new TaskStatus();
        status2.setName("Published");
        status2.setSlug("published");
        taskStatusRepository.save(status2);

        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("Draft"))
                .andExpect(jsonPath("$[0].slug").value("draft"))
                .andExpect(jsonPath("$[1].name").value("Published"))
                .andExpect(jsonPath("$[1].slug").value("published"));
    }

    @Test
    @WithMockUser
    void testGetTaskStatusById() throws Exception {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("In Progress");
        taskStatus.setSlug("in_progress");
        TaskStatus savedStatus = taskStatusRepository.save(taskStatus);

        mockMvc.perform(get("/api/task_statuses/" + savedStatus.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("In Progress"))
                .andExpect(jsonPath("$.slug").value("in_progress"))
                .andExpect(jsonPath("$.id").value(savedStatus.getId()));
    }

    @Test
    @WithMockUser
    void testCreateTaskStatus() throws Exception {
        TaskStatusCreateDTO taskStatusCreateDTO = new TaskStatusCreateDTO();
        taskStatusCreateDTO.setName("New Status");
        taskStatusCreateDTO.setSlug("new_status");

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("New Status"))
                .andExpect(jsonPath("$.slug").value("new_status"));

        var createdStatus = taskStatusRepository.findBySlug("new_status");
        assertThat(createdStatus).isPresent();
        assertThat(createdStatus.get().getName()).isEqualTo("New Status");
        assertThat(createdStatus.get().getSlug()).isEqualTo("new_status");
    }

    @Test
    @WithMockUser
    void testUpdateTaskStatus() throws Exception {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("Old Name");
        taskStatus.setSlug("old_slug");
        TaskStatus savedStatus = taskStatusRepository.save(taskStatus);

        TaskStatusUpdateDTO taskStatusUpdateDTO = new TaskStatusUpdateDTO();
        taskStatusUpdateDTO.setName(JsonNullable.of("Updated Name"));
        taskStatusUpdateDTO.setSlug(JsonNullable.of("updated_slug"));

        mockMvc.perform(put("/api/task_statuses/" + savedStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"))
                .andExpect(jsonPath("$.slug").value("updated_slug"));

        var updatedStatus = taskStatusRepository.findById(savedStatus.getId()).orElseThrow();
        assertThat(updatedStatus.getName()).isEqualTo("Updated Name");
        assertThat(updatedStatus.getSlug()).isEqualTo("updated_slug");
    }

    @Test
    @WithMockUser
    void testPartialUpdateTaskStatus() throws Exception {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("Original Name");
        taskStatus.setSlug("original_slug");
        TaskStatus savedStatus = taskStatusRepository.save(taskStatus);

        TaskStatusUpdateDTO taskStatusUpdateDTO = new TaskStatusUpdateDTO();
        taskStatusUpdateDTO.setName(JsonNullable.of("Partially Updated Name"));

        mockMvc.perform(put("/api/task_statuses/" + savedStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Partially Updated Name"))
                .andExpect(jsonPath("$.slug").value("original_slug"));

        var updatedStatus = taskStatusRepository.findById(savedStatus.getId()).orElseThrow();
        assertThat(updatedStatus.getName()).isEqualTo("Partially Updated Name");
        assertThat(updatedStatus.getSlug()).isEqualTo("original_slug");
    }

    @Test
    @WithMockUser
    void testDeleteTaskStatus() throws Exception {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("To Delete");
        taskStatus.setSlug("to_delete");
        TaskStatus savedStatus = taskStatusRepository.save(taskStatus);

        mockMvc.perform(delete("/api/task_statuses/" + savedStatus.getId()))
                .andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findById(savedStatus.getId())).isEmpty();
    }

    @Test
    @WithMockUser
    void testDeleteTaskStatusUsedInTask() throws Exception {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("In Progress");
        taskStatus.setSlug("in_progress");
        TaskStatus savedStatus = taskStatusRepository.save(taskStatus);

        Task task = new Task();
        task.setTitle("Test Task");
        task.setTaskStatus(savedStatus);
        task.setCreatedAt(LocalDate.now());
        taskRepository.save(task);

        mockMvc.perform(delete("/api/task_statuses/" + savedStatus.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void testCreateTaskStatusWithInvalidData() throws Exception {
        TaskStatusCreateDTO taskStatusCreateDTO = new TaskStatusCreateDTO();
        taskStatusCreateDTO.setName(""); // Пустое имя
        taskStatusCreateDTO.setSlug(""); // Пустой слаг

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testCreateTaskStatusWithDuplicateName() throws Exception {
        TaskStatus existingStatus = new TaskStatus();
        existingStatus.setName("Duplicate Name");
        existingStatus.setSlug("unique_slug");
        taskStatusRepository.save(existingStatus);

        TaskStatusCreateDTO taskStatusCreateDTO = new TaskStatusCreateDTO();
        taskStatusCreateDTO.setName("Duplicate Name");
        taskStatusCreateDTO.setSlug("another_slug");

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusCreateDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void testCreateTaskStatusWithDuplicateSlug() throws Exception {
        TaskStatus existingStatus = new TaskStatus();
        existingStatus.setName("Unique Name");
        existingStatus.setSlug("duplicate_slug");
        taskStatusRepository.save(existingStatus);

        TaskStatusCreateDTO taskStatusCreateDTO = new TaskStatusCreateDTO();
        taskStatusCreateDTO.setName("Another Name");
        taskStatusCreateDTO.setSlug("duplicate_slug");

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusCreateDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void testGetNonExistentTaskStatus() throws Exception {
        mockMvc.perform(get("/api/task_statuses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateNonExistentTaskStatus() throws Exception {
        TaskStatusUpdateDTO taskStatusUpdateDTO = new TaskStatusUpdateDTO();
        taskStatusUpdateDTO.setName(JsonNullable.of("Updated Name"));

        mockMvc.perform(put("/api/task_statuses/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteNonExistentTaskStatus() throws Exception {
        mockMvc.perform(delete("/api/task_statuses/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testTaskStatusDTOContainsCreatedAt() throws Exception {
        TaskStatus taskStatus = new TaskStatus();
        taskStatus.setName("Test Timestamp");
        taskStatus.setSlug("test_timestamp");
        TaskStatus savedStatus = taskStatusRepository.save(taskStatus);

        assertThat(savedStatus.getCreatedAt()).isNotNull();

        mockMvc.perform(get("/api/task_statuses/" + savedStatus.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Test Timestamp"))
                .andExpect(jsonPath("$.slug").value("test_timestamp"))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testAccessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isUnauthorized());

        TaskStatusCreateDTO taskStatusCreateDTO = new TaskStatusCreateDTO();
        taskStatusCreateDTO.setName("Test");
        taskStatusCreateDTO.setSlug("test");

        mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusCreateDTO)))
                .andExpect(status().isUnauthorized());
    }
}

package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.mapper.TaskStatusMapper;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.Comparator;
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
class TaskStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

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
        status1.setCreatedAt(LocalDate.now());
        taskStatusRepository.save(status1);

        TaskStatus status2 = new TaskStatus();
        status2.setName("Published");
        status2.setSlug("published");
        status2.setCreatedAt(LocalDate.now());
        taskStatusRepository.save(status2);

        MvcResult result = mockMvc.perform(get("/api/task_statuses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        List<TaskStatusDTO> statusesFromController = objectMapper.readValue(
                responseContent,
                new TypeReference<List<TaskStatusDTO>>() { }
        );

        List<TaskStatus> statusesFromDB = taskStatusRepository.findAll();

        assertThat(statusesFromController).hasSize(2);
        assertThat(statusesFromDB).hasSize(2);

        List<TaskStatusDTO> statusesFromDBasDTO = statusesFromDB.stream()
                .map(taskStatusMapper::map)
                .sorted(Comparator.comparing(TaskStatusDTO::getName))
                .toList();

        List<TaskStatusDTO> sortedStatusesFromController = statusesFromController.stream()
                .sorted(Comparator.comparing(TaskStatusDTO::getName))
                .toList();

        assertThat(sortedStatusesFromController)
                .usingRecursiveComparison()
                .ignoringFields("id")
                .isEqualTo(statusesFromDBasDTO);
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

        MvcResult result = mockMvc.perform(post("/api/task_statuses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        TaskStatusDTO statusFromController = objectMapper.readValue(responseContent, TaskStatusDTO.class);

        var statusFromDB = taskStatusRepository.findBySlug("new_status").orElse(null);

        assertThat(statusFromDB).isNotNull();
        assertThat(statusFromController.getName()).isEqualTo(statusFromDB.getName());
        assertThat(statusFromController.getSlug()).isEqualTo(statusFromDB.getSlug());
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

        MvcResult result = mockMvc.perform(put("/api/task_statuses/" + savedStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusUpdateDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        TaskStatusDTO statusFromController = objectMapper.readValue(responseContent, TaskStatusDTO.class);

        var statusFromDB = taskStatusRepository.findById(savedStatus.getId()).orElseThrow();

        assertThat(statusFromController.getName()).isEqualTo("Updated Name");
        assertThat(statusFromDB.getName()).isEqualTo("Updated Name");
        assertThat(statusFromController.getSlug()).isEqualTo(statusFromDB.getSlug());
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

        MvcResult result = mockMvc.perform(put("/api/task_statuses/" + savedStatus.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(taskStatusUpdateDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        TaskStatusDTO statusFromController = objectMapper.readValue(responseContent, TaskStatusDTO.class);

        var statusFromDB = taskStatusRepository.findById(savedStatus.getId()).orElseThrow();

        assertThat(statusFromController.getName()).isEqualTo("Partially Updated Name");
        assertThat(statusFromDB.getName()).isEqualTo("Partially Updated Name");
        assertThat(statusFromController.getSlug()).isEqualTo(statusFromDB.getSlug());
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
        taskStatusCreateDTO.setName("");
        taskStatusCreateDTO.setSlug("");

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

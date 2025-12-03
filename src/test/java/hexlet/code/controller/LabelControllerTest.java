package hexlet.code.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelUpdateDTO;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
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
class LabelControllerTest {

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

    private TaskStatus testStatus;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        labelRepository.deleteAll();
        taskStatusRepository.deleteAll();

        testStatus = new TaskStatus();
        testStatus.setName("Test Status");
        testStatus.setSlug("test_status");
        taskStatusRepository.save(testStatus);
    }

    @Test
    @WithMockUser
    void testGetAllLabels() throws Exception {
        Label label1 = new Label();
        label1.setName("bug");
        labelRepository.save(label1);

        Label label2 = new Label();
        label2.setName("feature");
        labelRepository.save(label2);

        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].name").value("bug"))
                .andExpect(jsonPath("$[1].name").value("feature"));
    }

    @Test
    @WithMockUser
    void testGetLabelById() throws Exception {
        Label label = new Label();
        label.setName("bug");
        Label savedLabel = labelRepository.save(label);

        mockMvc.perform(get("/api/labels/" + savedLabel.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("bug"))
                .andExpect(jsonPath("$.id").value(savedLabel.getId()));
    }

    @Test
    @WithMockUser
    void testCreateLabel() throws Exception {
        LabelCreateDTO labelCreateDTO = new LabelCreateDTO();
        labelCreateDTO.setName("new label");

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labelCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("new label"));

        var createdLabel = labelRepository.findByName("new label");
        assertThat(createdLabel).isPresent();
        assertThat(createdLabel.get().getName()).isEqualTo("new label");
    }

    @Test
    @WithMockUser
    void testUpdateLabel() throws Exception {
        Label label = new Label();
        label.setName("Old Name");
        Label savedLabel = labelRepository.save(label);

        LabelUpdateDTO labelUpdateDTO = new LabelUpdateDTO();
        labelUpdateDTO.setName(JsonNullable.of("Updated Name"));

        mockMvc.perform(put("/api/labels/" + savedLabel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labelUpdateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));

        var updatedLabel = labelRepository.findById(savedLabel.getId()).orElseThrow();
        assertThat(updatedLabel.getName()).isEqualTo("Updated Name");
    }

    @Test
    @WithMockUser
    void testDeleteLabel() throws Exception {
        Label label = new Label();
        label.setName("To Delete");
        Label savedLabel = labelRepository.save(label);

        mockMvc.perform(delete("/api/labels/" + savedLabel.getId()))
                .andExpect(status().isNoContent());

        assertThat(labelRepository.findById(savedLabel.getId())).isEmpty();
    }

    @Test
    @WithMockUser
    void testDeleteLabelUsedInTask() throws Exception {
        Label label = new Label();
        label.setName("bug");
        Label savedLabel = labelRepository.save(label);

        Task task = new Task();
        task.setTitle("Bug Task");
        task.setTaskStatus(testStatus);
        task.setLabels(List.of(savedLabel));
        task.setCreatedAt(LocalDate.now());
        taskRepository.save(task);

        mockMvc.perform(delete("/api/labels/" + savedLabel.getId()))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser
    void testCreateLabelWithInvalidData() throws Exception {
        LabelCreateDTO labelCreateDTO = new LabelCreateDTO();
        labelCreateDTO.setName("ab"); // Слишком короткое имя

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labelCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testCreateLabelWithDuplicateName() throws Exception {
        Label existingLabel = new Label();
        existingLabel.setName("Duplicate Name");
        labelRepository.save(existingLabel);

        LabelCreateDTO labelCreateDTO = new LabelCreateDTO();
        labelCreateDTO.setName("Duplicate Name");

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labelCreateDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    void testAccessWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/labels"))
                .andExpect(status().isUnauthorized());

        LabelCreateDTO labelCreateDTO = new LabelCreateDTO();
        labelCreateDTO.setName("Test");

        mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labelCreateDTO)))
                .andExpect(status().isUnauthorized());
    }
}

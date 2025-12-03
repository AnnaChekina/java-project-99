package hexlet.code.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.LabelCreateDTO;
import hexlet.code.dto.LabelDTO;
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

        MvcResult result = mockMvc.perform(get("/api/labels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        List<LabelDTO> labelsFromController = objectMapper.readValue(
                responseContent,
                new TypeReference<List<LabelDTO>>() { }
        );

        List<Label> labelsFromDB = labelRepository.findAll();

        assertThat(labelsFromController).hasSize(2);
        assertThat(labelsFromDB).hasSize(2);

        List<String> controllerLabelNames = labelsFromController.stream()
                .map(LabelDTO::getName)
                .sorted()
                .collect(Collectors.toList());

        List<String> dbLabelNames = labelsFromDB.stream()
                .map(Label::getName)
                .sorted()
                .collect(Collectors.toList());

        assertThat(controllerLabelNames).containsExactlyElementsOf(dbLabelNames);
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

        MvcResult result = mockMvc.perform(post("/api/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labelCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LabelDTO labelFromController = objectMapper.readValue(responseContent, LabelDTO.class);

        var labelFromDB = labelRepository.findByName("new label").orElse(null);

        assertThat(labelFromDB).isNotNull();
        assertThat(labelFromController.getName()).isEqualTo(labelFromDB.getName());
        assertThat(labelFromController.getId()).isEqualTo(labelFromDB.getId());
    }

    @Test
    @WithMockUser
    void testUpdateLabel() throws Exception {
        Label label = new Label();
        label.setName("Old Name");
        Label savedLabel = labelRepository.save(label);

        LabelUpdateDTO labelUpdateDTO = new LabelUpdateDTO();
        labelUpdateDTO.setName(JsonNullable.of("Updated Name"));

        MvcResult result = mockMvc.perform(put("/api/labels/" + savedLabel.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labelUpdateDTO)))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        LabelDTO labelFromController = objectMapper.readValue(responseContent, LabelDTO.class);

        var labelFromDB = labelRepository.findById(savedLabel.getId()).orElseThrow();

        assertThat(labelFromController.getName()).isEqualTo("Updated Name");
        assertThat(labelFromDB.getName()).isEqualTo("Updated Name");
        assertThat(labelFromController.getId()).isEqualTo(labelFromDB.getId());
    }

    @Test
    @WithMockUser
    void testDeleteLabel() throws Exception {
        Label label = new Label();
        label.setName("To Delete");
        Label savedLabel = labelRepository.save(label);

        assertThat(labelRepository.findById(savedLabel.getId())).isPresent();

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
        labelCreateDTO.setName("ab");

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
    @WithMockUser
    void testGetNonExistentLabel() throws Exception {
        mockMvc.perform(get("/api/labels/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testUpdateNonExistentLabel() throws Exception {
        LabelUpdateDTO labelUpdateDTO = new LabelUpdateDTO();
        labelUpdateDTO.setName(JsonNullable.of("Updated Name"));

        mockMvc.perform(put("/api/labels/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(labelUpdateDTO)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    void testDeleteNonExistentLabel() throws Exception {
        mockMvc.perform(delete("/api/labels/999"))
                .andExpect(status().isNotFound());
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

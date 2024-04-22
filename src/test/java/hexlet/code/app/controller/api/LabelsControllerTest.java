package hexlet.code.app.controller.api;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.label.LabelUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.Label;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.util.ModelUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@AutoConfigureMockMvc
public class LabelsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelUtils modelUtils;

    @Autowired
    private LabelRepository labelRepository;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private Label testLabel;

    @BeforeEach
    public void setUp() {
        testLabel = modelUtils.generateData().getLabel();
        labelRepository.save(testLabel);

        var testUser = modelUtils.getUser();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @AfterEach
    public void clean() {
        labelRepository.deleteById(testLabel.getId());
    }

    @Test
    public void testIndex() throws Exception {
        var request = get("/api/labels").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var labelData = new LabelCreateDTO();
        labelData.setName("NewTestLabel");

        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(labelData));
        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        Long id = om.readTree(body).get("id").asLong();
        var label = labelRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("testCreate in LabelsControllerTest failed"));
        assertThat(label.getName()).isEqualTo(labelData.getName());
        labelRepository.deleteById(id);
    }

    @Test
    public void testCreateWithInvalidData() throws Exception {
        var labelData = new LabelCreateDTO();
        labelData.setName("nl");
        var request = post("/api/labels")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(labelData));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/labels/{id}", testLabel.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var body = result.getResponse().getContentAsString();
        assertThatJson(body).and(b -> b.node("name").isEqualTo(testLabel.getName()));
    }

    @Test
    public void testShowWithInvalidId() throws Exception {
        var id = testLabel.getId();
        labelRepository.deleteById(id);
        var request = get("/api/labels/{id}", id).with(token);
        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    public void testShowAuthenticated() throws Exception {
        var request = get("/api/labels/{id}", testLabel.getId());
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {
        var id = testLabel.getId();
        var labelData = new LabelUpdateDTO();
        labelData.setName("NewTestLabel");
        var request = put("/api/labels/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(labelData));
        mockMvc.perform(request).andExpect(status().isOk());

        var updatedLabel = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("testUpdate in LabelsControllerTest failed"));
        assertThat(updatedLabel.getName()).isEqualTo(labelData.getName());
    }

    @Test
    public void updateWithInvalidData() throws Exception {
        var labelData = new LabelUpdateDTO();
        labelData.setName("nl");
        var request = put("/api/labels/{id}", testLabel.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(labelData));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testDelete() throws Exception {
        var id = testLabel.getId();
        assertThat(labelRepository.findById(id)).isPresent();
        var request = delete("/api/labels/{id}", id).with(token);
        mockMvc.perform(request).andExpect(status().isNoContent());
        assertThat(labelRepository.findById(id)).isEmpty();
    }
}

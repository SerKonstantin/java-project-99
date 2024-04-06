package hexlet.code.app.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.repository.TaskStatusRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskStatusesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    private JwtRequestPostProcessor token;

    private TaskStatus testTaskStatus;

    @BeforeEach
    public void setUp() {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        var taskData = Instancio.of(TaskStatusCreateDTO.class)
                .supply(Select.field(TaskStatusCreateDTO::getName), () -> "Test status name")
                .supply(Select.field(TaskStatusCreateDTO::getSlug), () -> "test_status_slug")
                .create();
        testTaskStatus = taskStatusMapper.map(taskData);
        taskStatusRepository.save(testTaskStatus);
    }

    @AfterEach
    public void clean() {
        taskStatusRepository.deleteById(testTaskStatus.getId());
    }

    @Test
    public void index() throws Exception {
        var request = get("/api/task_statuses").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var taskData = Map.of(
                "name", "New test status name",
                "slug", "new_test_slug"
        );

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskData));
        mockMvc.perform(request).andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findBySlug(taskData.get("slug"))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "\ntestCreate() in TaskStatusesControllerTest failed\n"
                ));
        assertThat(taskStatus.getName()).isEqualTo(taskData.get("name"));
    }

    @ParameterizedTest
    @CsvSource({
        "'NewName', null",
        "'NewName', ''",
        "'NewName', Optional.empty()",
        "null, 'new_slug'",
        "'', 'new_slug'",
        "Optional.empty(), 'new_slug'"
    })
    public void testCreateWithInvalidData(String name, String slug) throws Exception {
        var createData = Map.of(
                "name", name,
                "slug", slug
        );

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testShow() throws Exception {
        var id = testTaskStatus.getId();
        var request = get("/api/task_statuses/{id}", id).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();

        assertThatJson(body).and(
                b -> b.node("name").isEqualTo(testTaskStatus.getName()),
                b -> b.node("slug").isEqualTo(testTaskStatus.getSlug())
        );
    }

    @Test
    public void testShowWithInvalidId() throws Exception {
        var id = testTaskStatus.getId();
        taskStatusRepository.deleteById(id);
        var request = get("/api/task_statuses/{id}", id).with(token);
        mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    public void testShowNotAuthenticated() throws Exception {
        var request = get("/api/task_statuses/{id}", testTaskStatus.getId());
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {
        var id = testTaskStatus.getId();

        var updateData = Map.of(
                "name", "NewName",
                "slug", "new_slug"
        );

        var request = put("/api/task_statuses/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "\ntestUpdate in TaskStatusesControllerTest failed\n"
                ));

        assertThat(updatedTaskStatus.getName()).isEqualTo(updateData.get("name"));
        assertThat(updatedTaskStatus.getSlug()).isEqualTo(updateData.get("slug"));
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var id = testTaskStatus.getId();

        var updateData = Map.of(
                "name", "NewName"
        );

        var request = put("/api/task_statuses/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "\ntestUpdate in TaskStatusesControllerTest failed\n"
                ));

        assertThat(updatedTaskStatus.getName()).isEqualTo(updateData.get("name"));
        assertThat(updatedTaskStatus.getSlug()).isEqualTo(testTaskStatus.getSlug());
    }

    @ParameterizedTest
    @CsvSource({
            "'', 'new_slug'",
            "null, 'new_slug'",
            "Optional.empty(), 'new_slug'",
            "'NewName', ''",
            "'NewName', null",
            "'NewName', Optional.empty()"
    })
    public void testUpdateWithInvalidData(String name, String slug) throws Exception {
        var updateData = Map.of(
                "name", name,
                "slug", slug
        );

        var request = put("/api/task_statuses/{id}", testTaskStatus.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testDelete() throws Exception {
        var id = testTaskStatus.getId();

        assertThat(taskStatusRepository.findById(id)).isPresent();

        var request = delete("/api/task_statuses/{id}", id).with(token);
        mockMvc.perform(request).andExpect(status().isNoContent());

        assertThat(taskStatusRepository.findById(id)).isEmpty();
    }
}

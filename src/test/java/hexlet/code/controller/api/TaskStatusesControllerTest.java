package hexlet.code.controller.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.dto.task_status.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.util.ModelUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.stream.Stream;

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
    private ObjectMapper om;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private ModelUtils modelUtils;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private TaskStatus testTaskStatus;

    @BeforeEach
    public void setUp() {
        testTaskStatus = modelUtils.generateData().getTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        var testUser = modelUtils.getUser();
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @AfterEach
    public void clean() {
        taskStatusRepository.deleteById(testTaskStatus.getId());
    }

    @Test
    public void testIndex() throws Exception {
        var request = get("/api/task_statuses").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var taskData = new TaskStatusCreateDTO();
        taskData.setName("New test status name");
        taskData.setSlug("new_test_slug");

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(taskData));
        mockMvc.perform(request).andExpect(status().isCreated());

        var taskStatus = taskStatusRepository.findBySlug(taskData.getSlug())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "\ntestCreate() in TaskStatusesControllerTest failed\n"
                ));
        assertThat(taskStatus.getName()).isEqualTo(taskData.getName());
    }

    @ParameterizedTest
    @MethodSource("supplyWithCreateAndUpdateInvalidData")
    public void testCreateWithInvalidData(String name, String slug) throws Exception {
        var createData = new TaskStatusCreateDTO();
        createData.setName(name);
        createData.setSlug(slug);

        var request = post("/api/task_statuses")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    private static Stream<Arguments> supplyWithCreateAndUpdateInvalidData() {
        return Stream.of(
                Arguments.of("NewName", null),
                Arguments.of("NewName", ""),
                Arguments.of(null, "new_slug"),
                Arguments.of("", "new_slug")
        );
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
        var updateData = new TaskStatusUpdateDTO();
        updateData.setName(JsonNullable.of("NewName"));
        updateData.setSlug(JsonNullable.of("new_slug"));

        var request = put("/api/task_statuses/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "\ntestUpdate in TaskStatusesControllerTest failed\n"
                ));

        assertThat(updatedTaskStatus.getName()).isEqualTo(updateData.getName().get());
        assertThat(updatedTaskStatus.getSlug()).isEqualTo(updateData.getSlug().get());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var id = testTaskStatus.getId();
        var updateData = new TaskStatusUpdateDTO();
        updateData.setName(JsonNullable.of("NewName"));

        var request = put("/api/task_statuses/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedTaskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "\ntestUpdate in TaskStatusesControllerTest failed\n"
                ));

        assertThat(updatedTaskStatus.getName()).isEqualTo(updateData.getName().get());
        assertThat(updatedTaskStatus.getSlug()).isEqualTo(testTaskStatus.getSlug());
    }

    @ParameterizedTest
    @MethodSource("supplyWithCreateAndUpdateInvalidData")
    public void testUpdateWithInvalidData(String name, String slug) throws Exception {
        var updateData = new TaskStatusUpdateDTO();
        updateData.setName(JsonNullable.of(name));
        updateData.setSlug(JsonNullable.of(slug));

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

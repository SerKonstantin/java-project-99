package hexlet.code.app.controller.api;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.util.ModelUtils;
import net.datafaker.Faker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.stream.Stream;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Annotation required to use Faker in parametrized tests
public class TasksControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private Faker faker;

    @Autowired
    private ModelUtils modelUtils;

    private SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor token;
    private User testUser;
    private Task testTask;
    private TaskStatus testTaskStatus;

    @BeforeEach
    public void setUp() {
        modelUtils.generateData();

        testUser = modelUtils.getUser();
        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));

        testTaskStatus = modelUtils.getTaskStatus();
        taskStatusRepository.save(testTaskStatus);

        testTask = modelUtils.getTask();
        testTask.setTaskStatus(testTaskStatus);
        testTaskStatus.getTasks().add(testTask);
        testTask.setAssignee(testUser);
        testUser.getTasks().add(testTask);
        taskRepository.save(testTask);
        taskStatusRepository.save(testTaskStatus);
        userRepository.save(testUser);
    }

    @AfterEach
    public void clean() {
        taskRepository.deleteById(testTask.getId());
        taskStatusRepository.deleteById(testTaskStatus.getId());
        userRepository.deleteById(testUser.getId());
    }

    @Test
    public void testIndex() throws Exception {
        var request = get("/api/tasks").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var createData = new TaskCreateDTO();
        createData.setIndex(faker.number().numberBetween(1L, 10000L));
        createData.setAssigneeId(testUser.getId());
        createData.setTitle(faker.lorem().word());
        createData.setContent(faker.lorem().paragraph());
        createData.setStatus(testTaskStatus.getSlug());

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));

        var result = mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andReturn();

        var responseBody = result.getResponse().getContentAsString();
        Long id = om.readTree(responseBody).get("id").asLong();
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("\ntestCreate in TasksControllerTest failed\n"));

        assertThat(task.getName()).isEqualTo(createData.getTitle());
        assertThat(task.getIndex()).isEqualTo(createData.getIndex());
        assertThat(task.getDescription()).isEqualTo(createData.getContent());
        assertThat(task.getTaskStatus()).isEqualTo(testTaskStatus);
        assertThat(testTaskStatus.getTasks()).contains(task);
        assertThat(task.getAssignee()).isEqualTo(testUser);
        assertThat(testUser.getTasks()).contains(task);
    }

    @Test
    public void testCreateWithPartialData() throws Exception {
        var createData = new TaskCreateDTO();
        createData.setTitle(faker.lorem().word());
        createData.setStatus(testTaskStatus.getSlug());

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));
        mockMvc.perform(request).andExpect(status().isCreated());
    }

    @ParameterizedTest
    @MethodSource("supplyWithCreateAndUpdateInvalidData")
    public void testCreateWithInvalidData(String title, String statusSlug) throws Exception {
        var createData = new TaskCreateDTO();
        createData.setTitle(title);
        createData.setStatus(statusSlug);

        var request = post("/api/tasks")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));
        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    private Stream<Arguments> supplyWithCreateAndUpdateInvalidData() {
        return Stream.of(
                Arguments.of(faker.lorem().word(), ""),
                Arguments.of(faker.lorem().word(), null),
                Arguments.of("", faker.lorem().word()),
                Arguments.of(null, faker.lorem().word())
        );
    }

    @Test
    public void testShow() throws Exception {
        var request = get("/api/tasks/{id}", testTask.getId()).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();
        var responseBody = result.getResponse().getContentAsString();

        assertThatJson(responseBody).and(
                body -> body.node("index").isEqualTo(testTask.getIndex()),
                body -> body.node("assigneeId").isEqualTo(testUser.getId()),
                body -> body.node("title").isEqualTo(testTask.getName()),
                body -> body.node("content").isEqualTo(testTask.getDescription()),
                body -> body.node("status").isEqualTo(testTaskStatus.getSlug())
        );
    }

    @Test
    public void testShowWithInvalidId() throws Exception {
        var id = testTask.getId();
        taskRepository.deleteById(id);

        var request = get("/api/tasks/{id}", id).with(token);
        var result = mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    public void testShowNotAuthenticated() throws Exception {
        var request = get("/api/tasks/{id}", testTask.getId());
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {
        var updateData = new TaskUpdateDTO();
        updateData.setIndex(JsonNullable.of(faker.number().numberBetween(1L, 10000L)));
        updateData.setAssigneeId(JsonNullable.of(modelUtils.getUser().getId()));
        updateData.setTitle(JsonNullable.of(faker.lorem().word()));
        updateData.setContent(JsonNullable.of(faker.lorem().word()));
        updateData.setStatus(JsonNullable.of(modelUtils.getTaskStatus().getSlug()));

        var request = put("/api/tasks/{id}", testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());
        var updatedTask = taskRepository.findById(testTask.getId())
                .orElseThrow(() -> new ResourceNotFoundException("\ntestUpdate in TasksControllerTest failed\n"));

        assertThat(updatedTask.getIndex()).isEqualTo(updateData.getIndex().get());
        assertThat(updatedTask.getAssignee().getId()).isEqualTo(updateData.getAssigneeId().get());
        assertThat(updatedTask.getName()).isEqualTo(updateData.getTitle().get());
        assertThat(updatedTask.getDescription()).isEqualTo(updateData.getContent().get());
        assertThat(updatedTask.getTaskStatus().getSlug()).isEqualTo(updateData.getStatus().get());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var updateData = new TaskUpdateDTO();
        updateData.setAssigneeId(JsonNullable.of(modelUtils.getUser().getId()));

        var request = put("/api/tasks/{id}", testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());
        var updatedTask = taskRepository.findById(testTask.getId())
                .orElseThrow(() -> new ResourceNotFoundException("\ntestUpdate in TasksControllerTest failed\n"));

        assertThat(updatedTask.getIndex()).isEqualTo(testTask.getIndex());
        assertThat(updatedTask.getAssignee().getId()).isEqualTo(updateData.getAssigneeId().get());
        assertThat(updatedTask.getName()).isEqualTo(testTask.getName());
        assertThat(updatedTask.getDescription()).isEqualTo(testTask.getDescription());
        assertThat(updatedTask.getTaskStatus().getSlug()).isEqualTo(testTask.getTaskStatus().getSlug());
    }

    @ParameterizedTest
    @MethodSource("supplyWithCreateAndUpdateInvalidData")
    public void testUpdateWithInvalidData(String title, String statusSlug) throws Exception {
        var updateData = new TaskUpdateDTO();
        updateData.setTitle(JsonNullable.of(title));
        updateData.setStatus(JsonNullable.of(statusSlug));

        var request = put("/api/tasks/{id}", testTask.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testDelete() throws Exception {
        var id = testTask.getId();
        assertThat(taskRepository.findById(id)).isPresent();

        var request = delete("/api/tasks/{id}", id).with(token);
        mockMvc.perform(request).andExpect(status().isNoContent());
        assertThat(taskRepository.findById(id)).isEmpty();
    }
}

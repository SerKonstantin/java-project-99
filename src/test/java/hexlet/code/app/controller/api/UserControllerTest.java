package hexlet.code.app.controller.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.dto.user.UserUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.model.User;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.util.stream.Stream;

@SpringBootTest
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS) // Annotation required to use Faker in parametrized tests
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private Faker faker;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ModelUtils modelUtils;

    private JwtRequestPostProcessor token;
    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = modelUtils.generateData().getUser();
        userRepository.save(testUser);
        token = jwt().jwt(builder -> builder.subject(testUser.getEmail()));
    }

    @AfterEach
    public void clean() {
        userRepository.deleteById(testUser.getId());
    }

    @Test
    public void testIndex() throws Exception {
        var request = get("/api/users").with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    @Test
    public void testCreate() throws Exception {
        var createData = new UserCreateDTO();
        createData.setFirstName(faker.name().firstName());
        createData.setLastName(faker.name().lastName());
        createData.setEmail(faker.internet().emailAddress());
        createData.setPassword(faker.internet().password(3, 20));

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));

        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(createData.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("\ntestCreate() in UserControllerTest failed\n"));

        assertThat(user.getFirstName()).isEqualTo(createData.getFirstName());
        assertThat(user.getLastName()).isEqualTo(createData.getLastName());
        assertThat(user.getEncryptedPassword()).isNotEqualTo(createData.getPassword());
    }

    @Test
    public void testCreateWithoutNames() throws Exception {
        var createData = new UserCreateDTO();
        createData.setEmail(faker.internet().emailAddress());
        createData.setPassword(faker.internet().password(3, 20));

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));

        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(createData.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("\ntestCreate() in UserControllerTest failed\n"));

        assertThat(user.getFirstName()).isNull();
        assertThat(user.getLastName()).isNull();
        assertThat(user.getEncryptedPassword()).isNotEqualTo(createData.getPassword());
    }

    @ParameterizedTest
    @MethodSource("supplyWithCreateAndUpdateInvalidData")
    public void testCreateWithInvalidData(String email, String password) throws Exception {
        var createData = new UserCreateDTO();
        createData.setEmail(email);
        createData.setPassword(password);

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    private Stream<Arguments> supplyWithCreateAndUpdateInvalidData() {
        return Stream.of(
                Arguments.of(faker.internet().emailAddress(), "pw"),
                Arguments.of(faker.internet().emailAddress(), ""),
                Arguments.of(faker.internet().emailAddress(), null),
                Arguments.of("not_email", faker.internet().password(3, 20)),
                Arguments.of("", faker.internet().password(3, 20)),
                Arguments.of(null, faker.internet().password(3, 20))
        );
    }

    @Test
    public void testShow() throws Exception {
        var id = testUser.getId();
        var request = get("/api/users/{id}", id).with(token);
        var result = mockMvc.perform(request)
                .andExpect(status().isOk())
                .andReturn();

        var responseBody = result.getResponse().getContentAsString();

        assertThatJson(responseBody).and(
                body -> body.node("firstName").isEqualTo(testUser.getFirstName()),
                body -> body.node("lastName").isEqualTo(testUser.getLastName()),
                body -> body.node("email").isEqualTo(testUser.getEmail())
        );
    }

    @Test
    public void testShowWithInvalidId() throws Exception {
        var id = testUser.getId();
        userRepository.deleteById(id);

        var request = get("/api/users/{id}", id).with(token);
        var result = mockMvc.perform(request).andExpect(status().isNotFound());
    }

    @Test
    public void testShowNotAuthenticated() throws Exception {
        var request = get("/api/users/{id}", testUser.getId());
        mockMvc.perform(request).andExpect(status().isUnauthorized());
    }

    @Test
    public void testUpdate() throws Exception {
        var id = testUser.getId();

        var updateData = new UserUpdateDTO();
        updateData.setFirstName(JsonNullable.of(faker.name().firstName()));
        updateData.setLastName(JsonNullable.of(faker.name().lastName()));
        updateData.setEmail(JsonNullable.of(faker.internet().emailAddress()));
        updateData.setPassword(JsonNullable.of(faker.internet().password(3, 20)));

        var request = put("/api/users/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("\ntestUpdate in UserControllerTest failed\n"));

        assertThat(updatedUser.getEmail()).isEqualTo(updateData.getEmail().get());
        assertThat(updatedUser.getFirstName()).isEqualTo(updateData.getFirstName().get());
        assertThat(updatedUser.getLastName()).isEqualTo(updateData.getLastName().get());
        assertThat(updatedUser.getEncryptedPassword()).isNotEqualTo(updateData.getPassword().get());
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var id = testUser.getId();

        var updateData = new UserUpdateDTO();
        updateData.setFirstName(JsonNullable.of(faker.name().firstName()));

        var request = put("/api/users/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("\ntestUpdate in UserControllerTest failed\n"));

        assertThat(updatedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(updatedUser.getFirstName()).isEqualTo(updateData.getFirstName().get());
        assertThat(updatedUser.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(updatedUser.getEncryptedPassword()).isEqualTo(testUser.getPassword());
    }

    @ParameterizedTest
    @MethodSource("supplyWithCreateAndUpdateInvalidData")
    public void testUpdateWithInvalidData(String email, String password) throws Exception {
        var updateData = new UserUpdateDTO();
        updateData.setEmail(JsonNullable.of(email));
        updateData.setPassword(JsonNullable.of(password));

        var request = put("/api/users/{id}", testUser.getId())
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isBadRequest());
    }

    @Test
    public void testDelete() throws Exception {
        var id = testUser.getId();

        assertThat(userRepository.findById(id)).isPresent();

        var request = delete("/api/users/{id}", id).with(token);
        mockMvc.perform(request).andExpect(status().isNoContent());

        assertThat(userRepository.findById(id)).isEmpty();
    }
}

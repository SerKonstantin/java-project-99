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
import hexlet.code.app.dto.userDto.UserCreateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@AutoConfigureMockMvc
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
    private UserMapper userMapper;

    private JwtRequestPostProcessor token;

    private User testUser;

    @BeforeEach
    public void setUp() {
        token = jwt().jwt(builder -> builder.subject("hexlet@example.com"));
        var userData = Instancio.of(UserCreateDTO.class)
                .supply(Select.field(UserCreateDTO::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(UserCreateDTO::getLastName), () -> faker.name().lastName())
                .supply(Select.field(UserCreateDTO::getEmail), () -> faker.internet().emailAddress())
                // TODO mb hash the password?
                .supply(Select.field(UserCreateDTO::getPassword), () -> faker.internet().password(3, 20))
                .create();
        testUser = userMapper.map(userData);
        userRepository.save(testUser);
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
        var createData = Map.of(
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "email", faker.internet().emailAddress(),
                "password", faker.internet().password(3, 12)
        );

        var request = post("/api/users")
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(createData));

        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(createData.get("email"))
                .orElseThrow(() -> new ResourceNotFoundException("\ntestCreate() in UserControllerTest failed\n"));

        assertThat(user.getFirstName()).isEqualTo(createData.get("firstName"));
        assertThat(user.getLastName()).isEqualTo(createData.get("lastName"));
        assertThat(user.getHashedPassword()).isNotEqualTo(createData.get("password"));
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
    public void testUpdate() throws Exception {
        var id = testUser.getId();

        var updateData = Map.of(
                "firstName", faker.name().firstName(),
                "lastName", faker.name().lastName(),
                "email", faker.internet().emailAddress(),
                "password", faker.internet().password(3, 12)
        );

        var request = put("/api/users/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("\ntestUpdate in UserControllerTest failed\n"));

        assertThat(updatedUser.getEmail()).isEqualTo(updateData.get("email"));
        assertThat(updatedUser.getFirstName()).isEqualTo(updateData.get("firstName"));
        assertThat(updatedUser.getLastName()).isEqualTo(updateData.get("lastName"));
        assertThat(updatedUser.getHashedPassword()).isNotEqualTo(updateData.get("password"));
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var id = testUser.getId();

        var updateData = Map.of(
                "firstName", faker.name().firstName()
        );

        var request = put("/api/users/{id}", id)
                .with(token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("\ntestUpdate in UserControllerTest failed\n"));

        assertThat(updatedUser.getEmail()).isEqualTo(testUser.getEmail());
        assertThat(updatedUser.getFirstName()).isEqualTo(updateData.get("firstName"));
        assertThat(updatedUser.getLastName()).isEqualTo(testUser.getLastName());
        assertThat(updatedUser.getHashedPassword()).isEqualTo(testUser.getPassword());
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

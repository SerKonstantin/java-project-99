package hexlet.code.app.controller.api;

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
import hexlet.code.app.repository.UserRepository;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;

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

    @Test
    public void testIndex() throws Exception {
        var result = mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andReturn();

        var body = result.getResponse().getContentAsString();
        assertThatJson(body).isArray();
    }

    // TODO hash password
    private UserCreateDTO generateUserDTO() {
        return Instancio.of(UserCreateDTO.class)
                .supply(Select.field(UserCreateDTO::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(UserCreateDTO::getLastName), () -> faker.name().lastName())
                .supply(Select.field(UserCreateDTO::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(UserCreateDTO::getHashedPassword), () -> faker.internet().password(3, 20))
                .create();
    }

    @Test
    public void testCreate() throws Exception {
        var userData = generateUserDTO();

        var request = post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(userData));

        mockMvc.perform(request).andExpect(status().isCreated());

        var user = userRepository.findByEmail(userData.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("\ntestCreate() in UserControllerTest failed\n"));

        assertThat(user.getFirstName()).isEqualTo(userData.getFirstName());
        assertThat(user.getLastName()).isEqualTo(userData.getLastName());
        assertThat(user.getHashedPassword()).isEqualTo(userData.getHashedPassword());
    }

    @Test
    public void testShow() throws Exception {
        var userData = generateUserDTO();
        var user = userMapper.map(userData);
        userRepository.save(user);
        var id = user.getId();

        var result = mockMvc.perform(get("/api/users/{id}", id))
                .andExpect(status().isOk())
                .andReturn();

        var responseBody = result.getResponse().getContentAsString();

        assertThatJson(responseBody).and(
                body -> body.node("firstName").isEqualTo(userData.getFirstName()),
                body -> body.node("lastName").isEqualTo(userData.getLastName()),
                body -> body.node("email").isEqualTo(userData.getEmail())
        );

    }

    @Test
    public void testUpdate() throws Exception {
        var userData = generateUserDTO();
        var user = userMapper.map(userData);
        userRepository.save(user);
        var id = user.getId();

        var newEmail = faker.internet().emailAddress();
        var newFirstName = faker.name().firstName();
        var newLastName = faker.name().lastName();
        var newHashedPassword = faker.internet().password(3, 20);

        var updateData = new HashMap<>();
        updateData.put("email", newEmail);
        updateData.put("firstName", newFirstName);
        updateData.put("lastName", newLastName);
        updateData.put("hashedPassword", newHashedPassword);

        var request = put("/api/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("\ntestUpdate in UserControllerTest failed\n"));

        assertThat(updatedUser.getEmail()).isEqualTo(newEmail);
        assertThat(updatedUser.getFirstName()).isEqualTo(newFirstName);
        assertThat(updatedUser.getLastName()).isEqualTo(newLastName);
        assertThat(updatedUser.getHashedPassword()).isEqualTo(newHashedPassword);
    }

    @Test
    public void testPartialUpdate() throws Exception {
        var userData = generateUserDTO();
        var user = userMapper.map(userData);
        userRepository.save(user);
        var id = user.getId();

        var newFirstName = faker.name().firstName();

        var updateData = new HashMap<>();
        updateData.put("firstName", newFirstName);

        var request = put("/api/users/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(om.writeValueAsString(updateData));

        mockMvc.perform(request).andExpect(status().isOk());

        var updatedUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("\ntestUpdate in UserControllerTest failed\n"));

        assertThat(updatedUser.getEmail()).isEqualTo(userData.getEmail());
        assertThat(updatedUser.getFirstName()).isEqualTo(newFirstName);
        assertThat(updatedUser.getLastName()).isEqualTo(userData.getLastName());
        assertThat(updatedUser.getHashedPassword()).isEqualTo(userData.getHashedPassword());
    }

    @Test
    public void testDelete() throws Exception {
        var userData = generateUserDTO();
        var user = userMapper.map(userData);
        userRepository.save(user);
        var id = user.getId();

        assertThat(userRepository.findById(id)).isPresent();

        mockMvc.perform(delete("/api/users/{id}", id))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findById(id)).isEmpty();
    }

}

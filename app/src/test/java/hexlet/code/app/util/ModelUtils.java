package hexlet.code.app.util;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.Task;
import hexlet.code.app.model.TaskStatus;
import hexlet.code.app.model.User;
import lombok.Getter;
import net.datafaker.Faker;
import org.instancio.Instancio;
import org.instancio.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ModelUtils {

    @Autowired
    private Faker faker;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private TaskStatusMapper taskStatusMapper;

    @Autowired
    private TaskMapper taskMapper;

    private User user;
    private TaskStatus taskStatus;
    private Task task;

    public ModelUtils generateData() {
        var hashedPassword = passwordEncoder.encode(faker.internet().password(3, 20));
        var userData = Instancio.of(UserCreateDTO.class)
                .supply(Select.field(UserCreateDTO::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(UserCreateDTO::getLastName), () -> faker.name().lastName())
                .supply(Select.field(UserCreateDTO::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(UserCreateDTO::getPassword), () -> hashedPassword)
                .create();
        user = userMapper.map(userData);

        var taskStatusData = Instancio.of(TaskStatusCreateDTO.class)
                .supply(Select.field(TaskStatusCreateDTO::getName), () -> "Test status name")
                .supply(Select.field(TaskStatusCreateDTO::getSlug), () -> "test_status_slug")
                .create();
        taskStatus = taskStatusMapper.map(taskStatusData);

        var taskData = Instancio.of(TaskCreateDTO.class)
                .supply(Select.field(TaskCreateDTO::getIndex), () -> faker.number().numberBetween(1L, 10000L))
                .supply(Select.field(TaskCreateDTO::getAssigneeId), () -> user.getId())
                .supply(Select.field(TaskCreateDTO::getTitle), () -> faker.lorem().word())
                .supply(Select.field(TaskCreateDTO::getContent), () -> faker.lorem().paragraph())
                .supply(Select.field(TaskCreateDTO::getStatus), () -> taskStatus.getSlug())
                .create();
        task = taskMapper.map(taskData);

        return this;
    }
}

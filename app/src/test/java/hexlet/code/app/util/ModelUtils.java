package hexlet.code.app.util;

import hexlet.code.app.dto.label.LabelCreateDTO;
import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task_status.TaskStatusCreateDTO;
import hexlet.code.app.dto.user.UserCreateDTO;
import hexlet.code.app.mapper.LabelMapper;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.mapper.TaskStatusMapper;
import hexlet.code.app.mapper.UserMapper;
import hexlet.code.app.model.Label;
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

import java.util.HashSet;

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

    @Autowired
    private LabelMapper labelMapper;

    private User user;
    private TaskStatus taskStatus;
    private Task task;
    private Label label;

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
                .supply(Select.field(TaskStatusCreateDTO::getName), () -> faker.lorem().word())
                .supply(Select.field(TaskStatusCreateDTO::getSlug), () -> faker.lorem().word())
                .create();
        taskStatus = taskStatusMapper.map(taskStatusData);

        var labelData = Instancio.of(LabelCreateDTO.class)
                .supply(Select.field(LabelCreateDTO::getName), () -> faker.lorem().characters(3, 1000))
                .create();
        label = labelMapper.map(labelData);

        var taskData = Instancio.of(TaskCreateDTO.class)
                .supply(Select.field(TaskCreateDTO::getIndex), () -> faker.number().numberBetween(1L, 10000L))
                .supply(Select.field(TaskCreateDTO::getAssigneeId), () -> user.getId())
                .supply(Select.field(TaskCreateDTO::getTitle), () -> faker.lorem().word())
                .supply(Select.field(TaskCreateDTO::getContent), () -> faker.lorem().paragraph())
                .supply(Select.field(TaskCreateDTO::getStatus), () -> taskStatus.getSlug())
                .supply(Select.field(TaskCreateDTO::getLabelIds), () -> new HashSet<Long>())
                .create();
        taskData.getLabelIds().add(label.getId());
        task = taskMapper.map(taskData);

        return this;
    }
}

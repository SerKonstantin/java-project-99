package hexlet.code.app.util;

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
        createUser();
        createTaskStatus();
        createLabel();
        createTask();

        return this;
    }

    private void createUser() {
        var encodedPassword = passwordEncoder.encode(faker.internet().password(3, 20));
        user = Instancio.of(User.class)
                .ignore(Select.field(User::getId))
                .supply(Select.field(User::getFirstName), () -> faker.name().firstName())
                .supply(Select.field(User::getLastName), () -> faker.name().lastName())
                .supply(Select.field(User::getEmail), () -> faker.internet().emailAddress())
                .supply(Select.field(User::getEncryptedPassword), () -> encodedPassword)
                .ignore(Select.field(User::getCreatedAt))
                .ignore(Select.field(User::getUpdatedAt))
                .create();
    }

    private void createTaskStatus() {
        taskStatus = Instancio.of(TaskStatus.class)
                .ignore(Select.field(TaskStatus::getId))
                .supply(Select.field(TaskStatus::getName), () -> faker.lorem().word())
                .supply(Select.field(TaskStatus::getSlug), () -> faker.lorem().word())
                .ignore(Select.field(TaskStatus::getCreatedAt))
                .ignore(Select.field(TaskStatus::getTasks))
                .create();
    }

    private void createLabel() {
        label = Instancio.of(Label.class)
                .ignore(Select.field(Label::getId))
                .supply(Select.field(Label::getName), () -> faker.lorem().characters(3, 1000))
                .ignore(Select.field(Label::getTasks))
                .ignore(Select.field(Label::getCreatedAt))
                .create();
    }

    private void createTask() {
        task = Instancio.of(Task.class)
                .ignore(Select.field(Task::getId))
                .supply(Select.field(Task::getName), () -> faker.lorem().word())
                .supply(Select.field(Task::getIndex), () -> faker.number().numberBetween(1L, 10000L))
                .supply(Select.field(Task::getDescription), () -> faker.lorem().paragraph())
                .supply(Select.field(Task::getTaskStatus), () -> taskStatus)
                .supply(Select.field(Task::getAssignee), () -> user)
                .supply(Select.field(Task::getLabels), () -> new HashSet<Long>())
                .create();
        task.getLabels().add(label);
    }
}

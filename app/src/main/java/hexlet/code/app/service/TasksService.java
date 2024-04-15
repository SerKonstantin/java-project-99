package hexlet.code.app.service;

import hexlet.code.app.dto.task.TaskCreateDTO;
import hexlet.code.app.dto.task.TaskDTO;
import hexlet.code.app.dto.task.TaskParamsDTO;
import hexlet.code.app.dto.task.TaskUpdateDTO;
import hexlet.code.app.exception.ResourceNotFoundException;
import hexlet.code.app.mapper.TaskMapper;
import hexlet.code.app.repository.LabelRepository;
import hexlet.code.app.repository.TaskRepository;
import hexlet.code.app.repository.TaskStatusRepository;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.specification.task.TaskSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TasksService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private TaskMapper taskMapper;

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskSpecification taskSpecification;

    public List<TaskDTO> getAll(TaskParamsDTO params) {
        var specification = taskSpecification.build(params);
        var tasks = taskRepository.findAll(specification);
        return tasks.stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO getById(long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));
        return taskMapper.map(task);
    }

    @Transactional
    public TaskDTO create(TaskCreateDTO data) {
        var task = taskMapper.map(data);

        if (data.getAssigneeId() != null) {
            var user = userRepository.findById(data.getAssigneeId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cannot find user with id " + data.getAssigneeId()
                    ));
            task.setAssignee(user);
        }

        if (data.getStatus() != null) {
            var taskStatusSlug = data.getStatus();
            var taskStatus = taskStatusRepository.findBySlug(taskStatusSlug)
                    .orElseThrow(() -> new ResourceNotFoundException("Cannot find status with slug " + taskStatusSlug));
            task.setTaskStatus(taskStatus);
        }

        if (data.getLabelIds() != null && !data.getLabelIds().isEmpty()) {
            var labels = data.getLabelIds().stream()
                    .map(id -> labelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                            "Label with id" + id + "not found"
                    )))
                    .collect(Collectors.toSet());
            task.setLabels(labels);
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    @Transactional
    public TaskDTO update(TaskUpdateDTO data, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task with id " + id + " not found"));

        taskMapper.update(data, task);

        if (data.getAssigneeId() != null && data.getAssigneeId().get() != null) {
            var user = userRepository.findById(data.getAssigneeId().get())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cannot find user with id " + data.getAssigneeId().get()
                    ));
            task.setAssignee(user);
        }

        if (data.getStatus() != null && data.getStatus().get() != null) {
            var taskStatus = taskStatusRepository.findBySlug(data.getStatus().get())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Cannot find status with slug " + data.getStatus().get()
                    ));
            task.setTaskStatus(taskStatus);
        }

        if (data.getLabelIds() != null && data.getLabelIds().get() != null && !data.getLabelIds().get().isEmpty()) {
            var labels = data.getLabelIds().get().stream()
                    .map(labelId -> labelRepository.findById(labelId).orElseThrow(() -> new ResourceNotFoundException(
                            "Label with id" + labelId + "not found"
                    )))
                    .collect(Collectors.toSet());
            task.setLabels(labels);
        }

        taskRepository.save(task);
        return taskMapper.map(task);
    }

    public void delete(Long id) {
        taskRepository.deleteById(id);
    }
}

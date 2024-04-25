package hexlet.code.mapper;

import hexlet.code.dto.task.TaskCreateDTO;
import hexlet.code.dto.task.TaskDTO;
import hexlet.code.dto.task.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(
        uses = { JsonNullableMapper.class, ReferenceMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private LabelRepository labelRepository;

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "taskStatus", source = "status")
    @Mapping(target = "labels", source = "taskLabelIds")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "title", source = "name")
    @Mapping(target = "content", source = "description")
    @Mapping(target = "status", source = "taskStatus.slug")
    @Mapping(target = "taskLabelIds", source = "labels")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "assignee", source = "assigneeId")
    @Mapping(target = "name", source = "title")
    @Mapping(target = "description", source = "content")
    @Mapping(target = "taskStatus", source = "status")
    @Mapping(target = "labels", source = "taskLabelIds")
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    protected TaskStatus taskStatusFromSlug(String slug) {
        return taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Slug " + slug + " not found"));
    }

    protected Set<Label> labelsFromIds(Set<Long> taskLabelIds) {
        if (taskLabelIds == null || taskLabelIds.isEmpty() || taskLabelIds.contains(null)) {
            return new HashSet<>();
        }
        return taskLabelIds.stream()
                .map(id -> labelRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                        "Label with id " + id + " not found"
                )))
                .collect(Collectors.toSet());
    }

    protected Set<Label> labelsFromNullables(JsonNullable<Set<Long>> nullableLabelIds) {
        return nullableLabelIds == null ? new HashSet<>() :  labelsFromIds(nullableLabelIds.get());
    }

    protected Set<Long> labelIdsFromLabels(Set<Label> labels) {
        if (labels == null || labels.isEmpty() || labels.contains(null)) {
            return new HashSet<>();
        }
        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toSet());
    }

    protected Set<Long> labelIdsFromNullables(JsonNullable<Set<Label>> nullableLabels) {
        return nullableLabels == null ? new HashSet<>() : labelIdsFromLabels(nullableLabels.get());
    }
}

package hexlet.code.specification.task;

import hexlet.code.dto.task.TaskParamsDTO;
import hexlet.code.model.Task;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class TaskSpecification {
    public Specification<Task> build(TaskParamsDTO params) {
        return withTitleCont(params.getTitleCont())
                .and(withAssigneeId(params.getAssigneeId()))
                .and(withStatus(params.getStatus()))
                .and(withLabelId(params.getLabelId()));
    }

    private Specification<Task> withTitleCont(String str) {
        return (root, query, cb) -> {
            if (str == null) {
                return cb.conjunction();
            } else {
                String pattern = "%" + str.toLowerCase() + "%";
                return cb.like(cb.lower(root.get("name")), pattern);
            }
        };
    }

    private Specification<Task> withAssigneeId(Long id) {
        return (root, query, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("assignee").get("id"), id);
    }

    private Specification<Task> withStatus(String slug) {
        return (root, query, cb) -> {
            return slug == null ? cb.conjunction() : cb.equal(cb.lower(root.get("taskStatus").get("slug")), slug);
        };
    }

    private Specification<Task> withLabelId(Long id) {
        return (root, query, cb) -> id == null ? cb.conjunction() : cb.equal(root.get("labels").get("id"), id);
    }
}

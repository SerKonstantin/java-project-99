package hexlet.code.app.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private Long index;
    private LocalDate createdAt;
    private String title;
    private String content;
    private String status;
    private Set<Long> taskLabelIds;

    @JsonProperty("assignee_id")
    private Long assigneeId;
}

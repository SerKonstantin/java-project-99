package hexlet.code.app.dto.task;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class TaskCreateDTO {
    private Long index;
    private String content;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    @NotBlank
    private String title;

    @NotBlank
    private String status;

    private Set<Long> taskLabelIds;
}

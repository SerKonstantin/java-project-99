package hexlet.code.app.dto.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateDTO {
    private Long index;
    private Long assigneeId;
    private String content;

    @NotBlank
    private String title;

    @NotBlank
    private String status;
}

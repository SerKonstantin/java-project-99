package hexlet.code.app.dto.task;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateDTO {
    private Long index;
    private Long assigneeId;
    private String title;
    private String content;
    private String status;
}

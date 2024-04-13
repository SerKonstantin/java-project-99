package hexlet.code.app.dto.task;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

import java.util.Set;

@Getter
@Setter
public class TaskUpdateDTO {
    private JsonNullable<Long> index;
    private JsonNullable<Long> assigneeId;
    private JsonNullable<String> content;

    @NotBlank
    private JsonNullable<String> title;

    @NotBlank
    private JsonNullable<String> status;

    private JsonNullable<Set<Long>> labelIds;
}

package hexlet.code.app.dto.task;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.openapitools.jackson.nullable.JsonNullable;

@Getter
@Setter
public class TaskUpdateDTO {
    private JsonNullable<Long> index;
    private JsonNullable<Long> assigneeId;
    private JsonNullable<String> content;

    @NotBlank
    private JsonNullable<String> title;

    @NotNull
    private JsonNullable<String> status;
}

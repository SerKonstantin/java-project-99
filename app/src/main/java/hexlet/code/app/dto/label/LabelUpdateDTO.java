package hexlet.code.app.dto.label;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LabelUpdateDTO {
    @NotBlank
    @Size(min = 3, max = 1000)
    private String name;
}

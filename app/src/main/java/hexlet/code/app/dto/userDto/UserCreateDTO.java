package hexlet.code.app.dto.userDto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateDTO {

    @NotBlank
    @Email
    private String email;

    private String firstName;

    private String lastName;

    @Size(min = 3)
    private String hashedPassword;
}

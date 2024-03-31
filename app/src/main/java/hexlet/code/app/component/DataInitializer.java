package hexlet.code.app.component;

import hexlet.code.app.model.User;
import hexlet.code.app.repository.UserRepository;
import hexlet.code.app.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final CustomUserDetailsService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void run(ApplicationArguments args) throws Exception {
        var email = "hexlet@example.com";
        var password = "qwerty";
        var userData = new User();
        userData.setEmail(email);
        userData.setHashedPassword(password);
        userService.createUser(userData);

        // TODO create faker users with tasks and assignments
    }

}

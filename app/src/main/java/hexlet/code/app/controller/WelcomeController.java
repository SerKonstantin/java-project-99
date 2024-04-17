package hexlet.code.app.controller;

import io.sentry.Sentry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WelcomeController {
    @GetMapping(path = "/welcome")
    public String welcome() {
        try {
            throw new Exception("This is a sentry test.");
        } catch (Exception e) {
            Sentry.captureException(e);
        }

        return "Welcome to Spring";
    }
}

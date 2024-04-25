package hexlet.code.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ManualSentryTest {
    @GetMapping(path = "/sentry")
    public String index() throws Exception {
        throw new Exception("Intentional exception throw for a sentry test.");
    }
}

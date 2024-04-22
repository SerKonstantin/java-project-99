package hexlet.code.app;

import io.sentry.Sentry;
import net.datafaker.Faker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AppApplication {

    public static void main(String[] args) {
        try {
            SpringApplication.run(AppApplication.class, args);
        } catch (Exception e) {
            Sentry.captureException(e);
        }
    }

    @Bean
    public Faker getFaker() {
        return new Faker();
    }

}

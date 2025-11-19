package hexlet.code.component;

import hexlet.code.model.User;
import hexlet.code.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CustomUserDetailsService userDetailsService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var email = "hexlet@example.com";

        if (!userDetailsService.userExists(email)) {
            var user = new User();
            user.setEmail(email);
            user.setFirstName("admin");

            userDetailsService.createUserWithRawPassword(user, "qwerty");
        }
    }
}

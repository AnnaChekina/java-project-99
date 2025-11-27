package hexlet.code.component;

import hexlet.code.model.Label;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CustomUserDetailsService userDetailsService;
    private final TaskStatusRepository taskStatusRepository;
    private final LabelRepository labelRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        var email = "hexlet@example.com";

        if (!userDetailsService.userExists(email)) {
            var user = new User();
            user.setEmail(email);
            user.setFirstName("admin");

            userDetailsService.createUserWithRawPassword(user, "qwerty");
        }

        createDefaultTaskStatus("Draft", "draft");
        createDefaultTaskStatus("ToReview", "to_review");
        createDefaultTaskStatus("ToBeFixed", "to_be_fixed");
        createDefaultTaskStatus("ToPublish", "to_publish");
        createDefaultTaskStatus("Published", "published");

        createDefaultLabel("bug");
        createDefaultLabel("feature");
    }

    private void createDefaultTaskStatus(String name, String slug) {
        if (taskStatusRepository.findBySlug(slug).isEmpty()) {
            var taskStatus = new TaskStatus();
            taskStatus.setName(name);
            taskStatus.setSlug(slug);
            taskStatusRepository.save(taskStatus);
        }
    }

    private void createDefaultLabel(String name) {
        if (labelRepository.findByName(name).isEmpty()) {
            var label = new Label();
            label.setName(name);
            labelRepository.save(label);
        }
    }
}

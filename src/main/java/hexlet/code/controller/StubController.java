package hexlet.code.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api")
public class StubController {

    @GetMapping("/labels")
    @ResponseStatus(HttpStatus.OK)
    public List<Object> getLabels(jakarta.servlet.http.HttpServletResponse response) {
        response.setHeader("X-Total-Count", "0");
        return Collections.emptyList();
    }
}

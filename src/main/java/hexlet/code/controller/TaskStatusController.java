    package hexlet.code.controller;

    import hexlet.code.dto.TaskStatusCreateDTO;
    import hexlet.code.dto.TaskStatusDTO;
    import hexlet.code.dto.TaskStatusUpdateDTO;
    import hexlet.code.service.TaskStatusService;
    import jakarta.validation.Valid;
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.http.HttpStatus;
    import org.springframework.web.bind.annotation.DeleteMapping;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.PutMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.ResponseStatus;
    import org.springframework.web.bind.annotation.RestController;

    import java.util.List;

    @RestController
    @RequestMapping("/api/task_statuses")
    public class TaskStatusController {

        @Autowired
        private TaskStatusService taskStatusService;

        @GetMapping
        @ResponseStatus(HttpStatus.OK)
        public List<TaskStatusDTO> index(jakarta.servlet.http.HttpServletResponse response) {
            var taskStatuses = taskStatusService.getAll();
            response.setHeader("X-Total-Count", String.valueOf(taskStatuses.size()));
            return taskStatuses;
        }

        @GetMapping("/{id}")
        @ResponseStatus(HttpStatus.OK)
        public TaskStatusDTO show(@PathVariable Long id) {
            return taskStatusService.findById(id);
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        public TaskStatusDTO create(@Valid @RequestBody TaskStatusCreateDTO taskStatusData) {
            return taskStatusService.create(taskStatusData);
        }

        @PutMapping("/{id}")
        @ResponseStatus(HttpStatus.OK)
        public TaskStatusDTO update(@Valid @RequestBody TaskStatusUpdateDTO taskStatusData, @PathVariable Long id) {
            return taskStatusService.update(taskStatusData, id);
        }

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        public void delete(@PathVariable Long id) {
            taskStatusService.delete(id);
        }
    }

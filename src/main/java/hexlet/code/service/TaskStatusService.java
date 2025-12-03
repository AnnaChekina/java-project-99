package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.model.TaskStatus;

import java.util.List;

public interface TaskStatusService {
    List<TaskStatusDTO> getAll();
    TaskStatusDTO findById(Long id);
    TaskStatusDTO create(TaskStatusCreateDTO taskStatusData);
    TaskStatusDTO update(TaskStatusUpdateDTO taskStatusData, Long id);
    void delete(Long id);
    TaskStatus findBySlug(String slug);
}

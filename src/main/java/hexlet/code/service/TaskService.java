package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TaskService {
    List<TaskDTO> getAll();
    Page<TaskDTO> getFiltered(TaskParamsDTO params, Pageable pageable);
    TaskDTO findById(Long id);
    TaskDTO create(TaskCreateDTO taskData);
    TaskDTO update(TaskUpdateDTO taskData, Long id);
    void delete(Long id);
}

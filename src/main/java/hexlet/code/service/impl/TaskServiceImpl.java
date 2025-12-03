package hexlet.code.service.impl;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskParamsDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.model.Task;
import hexlet.code.repository.TaskRepository;
import hexlet.code.service.TaskService;
import hexlet.code.specification.TaskSpecification;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskSpecification taskSpecification;

    @Override
    public List<TaskDTO> getAll() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(taskMapper::map)
                .toList();
    }

    @Override
    public Page<TaskDTO> getFiltered(TaskParamsDTO params, Pageable pageable) {
        Specification<Task> spec = taskSpecification.build(params);
        Page<Task> tasks = taskRepository.findAll(spec, pageable);
        return tasks.map(taskMapper::map);
    }

    @Override
    public TaskDTO findById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found: " + id));
        return taskMapper.map(task);
    }

    @Override
    public TaskDTO create(TaskCreateDTO taskData) {
        Task task = taskMapper.map(taskData);
        try {
            taskRepository.save(task);
            return taskMapper.map(task);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Task creation failed due to data integrity violation");
        }
    }

    @Override
    public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found: " + id));
        taskMapper.update(taskData, task);
        try {
            taskRepository.save(task);
            return taskMapper.map(task);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Task update failed due to data integrity violation");
        }
    }

    @Override
    public void delete(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found: " + id));
        taskRepository.delete(task);
    }
}

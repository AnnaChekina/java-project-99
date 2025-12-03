package hexlet.code.service;

import hexlet.code.dto.TaskStatusCreateDTO;
import hexlet.code.dto.TaskStatusDTO;
import hexlet.code.dto.TaskStatusUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskStatusMapper;
import hexlet.code.model.TaskStatus;
import hexlet.code.repository.TaskStatusRepository;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TaskStatusService {

    private final TaskStatusRepository taskStatusRepository;
    private final TaskStatusMapper taskStatusMapper;
    private final TaskService taskService;

    public List<TaskStatusDTO> getAll() {
        var taskStatuses = taskStatusRepository.findAll();
        return taskStatuses.stream()
                .map(taskStatusMapper::map)
                .toList();
    }

    public TaskStatusDTO findById(Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus Not Found: " + id));
        return taskStatusMapper.map(taskStatus);
    }

    public TaskStatusDTO create(TaskStatusCreateDTO taskStatusData) {
        var taskStatus = taskStatusMapper.map(taskStatusData);

        try {
            taskStatusRepository.save(taskStatus);
            return taskStatusMapper.map(taskStatus);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("TaskStatus with this name or slug already exists");
        }
    }

    public TaskStatusDTO update(TaskStatusUpdateDTO taskStatusData, Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus Not Found: " + id));

        taskStatusMapper.update(taskStatusData, taskStatus);

        try {
            taskStatusRepository.save(taskStatus);
            return taskStatusMapper.map(taskStatus);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("TaskStatus with this name or slug already exists");
        }
    }

    public void delete(Long id) {
        var taskStatus = taskStatusRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus Not Found: " + id));

        if (taskService.existsByTaskStatusId(id)) {
            throw new DataIntegrityViolationException("Cannot delete task status: status is used in tasks");
        }

        taskStatusRepository.delete(taskStatus);
    }

    public TaskStatus findBySlug(String slug) {
        return taskStatusRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("TaskStatus Not Found with slug: " + slug));
    }
}

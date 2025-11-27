package hexlet.code.service;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.mapper.TaskMapper;
import hexlet.code.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskMapper taskMapper;

    public List<TaskDTO> getAll() {
        var tasks = taskRepository.findAll();
        return tasks.stream()
                .map(taskMapper::map)
                .toList();
    }

    public TaskDTO findById(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found: " + id));
        return taskMapper.map(task);
    }

    public TaskDTO create(TaskCreateDTO taskData) {
        var task = taskMapper.map(taskData);
        try {
            taskRepository.save(task);
            return taskMapper.map(task);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Task creation failed due to data integrity violation");
        }
    }

    public TaskDTO update(TaskUpdateDTO taskData, Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found: " + id));
        taskMapper.update(taskData, task);
        try {
            taskRepository.save(task);
            return taskMapper.map(task);
        } catch (DataIntegrityViolationException e) {
            throw new DataIntegrityViolationException("Task update failed due to data integrity violation");
        }
    }

    public void delete(Long id) {
        var task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task Not Found: " + id));
        taskRepository.delete(task);
    }

    public boolean existsByAssigneeId(Long assigneeId) {
        return taskRepository.existsByAssigneeId(assigneeId);
    }

    public boolean existsByTaskStatusId(Long taskStatusId) {
        return taskRepository.existsByTaskStatusId(taskStatusId);
    }

    public boolean existsByLabelId(Long labelId) {
        return taskRepository.existsByLabelId(labelId);
    }
}

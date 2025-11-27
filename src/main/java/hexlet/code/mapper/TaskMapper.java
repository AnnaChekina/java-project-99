package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.TaskStatusRepository;
import hexlet.code.repository.UserRepository;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(
        uses = { JsonNullableMapper.class },
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = MappingConstants.ComponentModel.SPRING,
        unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public abstract class TaskMapper {

    @Autowired
    private TaskStatusRepository taskStatusRepository;

    @Autowired
    private UserRepository userRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "taskStatus", source = "status", qualifiedByName = "statusSlugToTaskStatus")
    @Mapping(target = "assignee", source = "assigneeId", qualifiedByName = "assigneeIdToUser")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "status", source = "taskStatus.slug")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "taskStatus", source = "status", qualifiedByName = "statusSlugToTaskStatus")
    @Mapping(target = "assignee", source = "assigneeId", qualifiedByName = "assigneeIdToUser")
    public abstract void update(TaskUpdateDTO dto, @MappingTarget Task model);

    @Named("statusSlugToTaskStatus")
    public TaskStatus statusSlugToTaskStatus(String statusSlug) {
        if (statusSlug == null) {
            return null;
        }
        return taskStatusRepository.findBySlug(statusSlug)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "TaskStatus not found with slug: " + statusSlug));
    }

    @Named("assigneeIdToUser")
    public User assigneeIdToUser(Long assigneeId) {
        return assigneeId == null ? null : userRepository.findById(assigneeId).orElse(null);
    }
}

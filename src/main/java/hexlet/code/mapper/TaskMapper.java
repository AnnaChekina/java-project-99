package hexlet.code.mapper;

import hexlet.code.dto.TaskCreateDTO;
import hexlet.code.dto.TaskDTO;
import hexlet.code.dto.TaskUpdateDTO;
import hexlet.code.exception.ResourceNotFoundException;
import hexlet.code.model.Label;
import hexlet.code.model.Task;
import hexlet.code.model.TaskStatus;
import hexlet.code.model.User;
import hexlet.code.repository.LabelRepository;
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

import java.util.List;
import java.util.stream.Collectors;

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

    @Autowired
    private LabelRepository labelRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "taskStatus", source = "status", qualifiedByName = "statusSlugToTaskStatus")
    @Mapping(target = "assignee", source = "assigneeId", qualifiedByName = "assigneeIdToUser")
    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "labelIdsToLabels")
    public abstract Task map(TaskCreateDTO dto);

    @Mapping(target = "assigneeId", source = "assignee.id")
    @Mapping(target = "status", source = "taskStatus.slug")
    @Mapping(target = "taskLabelIds", source = "labels", qualifiedByName = "labelsToLabelIds")
    public abstract TaskDTO map(Task model);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "taskStatus", source = "status", qualifiedByName = "statusSlugToTaskStatus")
    @Mapping(target = "assignee", source = "assigneeId", qualifiedByName = "assigneeIdToUser")
    @Mapping(target = "labels", source = "taskLabelIds", qualifiedByName = "labelIdsToLabels")
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

    @Named("labelIdsToLabels")
    public List<Label> labelIdsToLabels(List<Long> labelIds) {
        if (labelIds == null) {
            return List.of();
        }
        return labelRepository.findAllById(labelIds);
    }

    @Named("labelsToLabelIds")
    public List<Long> labelsToLabelIds(List<Label> labels) {
        if (labels == null) {
            return List.of();
        }
        return labels.stream()
                .map(Label::getId)
                .collect(Collectors.toList());
    }
}

package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskCreateDTO {

    @NotBlank(message = "не должно быть пустым")
    @Size(min = 1)
    private String title;

    private Integer index;

    private String content;

    @NotNull(message = "не должно равняться null")
    private String status;

    @JsonProperty("assignee_id")
    private Long assigneeId;
}

package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class TaskDTO {
    private Long id;
    private String title;
    private Integer index;
    private String content;

    @JsonProperty("assignee_id")
    private Long assigneeId;

    private String status;

    @JsonProperty("taskLabelIds")
    private List<Long> taskLabelIds;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate createdAt;
}

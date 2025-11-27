package hexlet.code.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.openapitools.jackson.nullable.JsonNullable;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class TaskUpdateDTO {

    @Size(min = 1)
    private JsonNullable<String> title = JsonNullable.undefined();

    private JsonNullable<Integer> index = JsonNullable.undefined();

    private JsonNullable<String> content = JsonNullable.undefined();

    private JsonNullable<String> status = JsonNullable.undefined();

    @JsonProperty("assignee_id")
    private JsonNullable<Long> assigneeId = JsonNullable.undefined();

    @JsonProperty("taskLabelIds")
    private JsonNullable<List<Long>> taskLabelIds = JsonNullable.undefined();
}

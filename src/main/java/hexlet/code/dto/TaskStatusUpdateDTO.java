package hexlet.code.dto;

import org.openapitools.jackson.nullable.JsonNullable;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TaskStatusUpdateDTO {

    @Size(min = 1)
    private JsonNullable<String> name = JsonNullable.undefined();

    @Size(min = 1)
    private JsonNullable<String> slug = JsonNullable.undefined();
}

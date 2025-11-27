package hexlet.code.dto;

import org.openapitools.jackson.nullable.JsonNullable;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LabelUpdateDTO {

    @Size(min = 3, max = 1000)
    private JsonNullable<String> name = JsonNullable.undefined();
}

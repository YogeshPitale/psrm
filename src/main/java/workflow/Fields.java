package workflow;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Fields {

    private String name;
    private FieldType fieldType;
    private DataType dataType;
    private Node node;
}

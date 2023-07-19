package workflow;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
public abstract class Node {

    private int id;
    private String name;
    private Enum<NodeType> type;
    private Workflow workflow;
    private Optional<Node> previousNode;
    private Optional<Node> nextNode;

    public abstract JSONObject process(JSONObject input);

}

package workflow;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONObject;

import java.util.List;

@Data
@NoArgsConstructor
public class MongoNode extends Node {

    private String query;
    private List<String> queryParameters;
    private Node nextNode;

    @Override
    public JSONObject process(JSONObject input) {
        return null;
    }

}

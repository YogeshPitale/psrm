package workflow;

import org.json.JSONObject;

import java.util.*;

public class ConditionNode extends Node{

    private String input;
    private String condition;
    List<Node> possibleNodes = new ArrayList<>();
    Map<String,Node> choiceMap = new HashMap<>();

    @Override
    public JSONObject process(JSONObject input) {
        return null;
    }
}

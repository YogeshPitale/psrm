package workflow;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;

@Data
@NoArgsConstructor
@Builder
public class Workflow {

    private int id;
    private String name;
    String startingPath;
    Node endingNode;
    HashMap<String,LinkedList<Node>> paths = new HashMap<>();

    public void addPath(String path, LinkedList<Node> nodeLinkedList){
        paths.put(path,nodeLinkedList);
    }

    public LinkedList<Node> addToLinkedList(Node... nodes){
        LinkedList<Node> nodeLinkedList = new LinkedList<>();
        for(Node node :nodes){
            nodeLinkedList.addLast(node);
        }
        return nodeLinkedList;
    }
}

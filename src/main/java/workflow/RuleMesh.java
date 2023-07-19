package workflow;

import org.json.JSONObject;

import java.util.*;

public class RuleMesh {

    public static void main(String[] args) {
        JSONObject input = null;
        Workflow stpFlow = setup();
        String currenPath=stpFlow.getStartingPath();
        HashMap<String, LinkedList<Node>> workflowPaths = stpFlow.getPaths();
        LinkedList<Node> currentPath =workflowPaths.get(currenPath);
        Node currentNode = currentPath.pop();
        while (!currentNode.getType().equals(NodeType.END)){
            JSONObject currentNodeOP=currentNode.process(input);
            if(currentNode.getType().equals(NodeType.CONDITIONAL))
            {
                String nextPath ="";  //Todo : Logic to extract next path from op of conditional node
                currentPath=workflowPaths.get(nextPath);
             }
            currentNode=currentPath.pop();
            input=currentNodeOP;
            if(currentNode.getType().equals(NodeType.ERROR)) {
                throw new RuntimeException("Exception occurred while executing node:" + currentNode.getName());
            }
        }
    }


    public static Workflow setup(){
        Workflow stpWorkFlow = new Workflow();

        Node startNode = Node.builder().id(1).name("Start").type(NodeType.START).build();
        Node validate_rtn = Node.builder().id(2).name("Validate RTN").type(NodeType.API).build();
        Node is_valid = Node.builder().id(2).name("Is Valid").type(NodeType.CONDITIONAL).build();
        Node autocorrect_rtn = Node.builder().id(3).name("Autocorrect RTN").type(NodeType.API).build();
        Node isSTP = Node.builder().id(4).name("isSTP").type(NodeType.MONGO).build();
        Node endNode = Node.builder().id(1).name("End").type(NodeType.START).build();

        LinkedList<Node> path1=stpWorkFlow.addToLinkedList(startNode,validate_rtn,is_valid);
        LinkedList<Node> path2=stpWorkFlow.addToLinkedList(isSTP,endNode);
        LinkedList<Node> path3=stpWorkFlow.addToLinkedList(autocorrect_rtn,isSTP,endNode);

        stpWorkFlow.addPath("path1",path1);
        stpWorkFlow.addPath("path2",path2);
        stpWorkFlow.addPath("path3",path3);

        stpWorkFlow.setStartingPath("path1");

        return stpWorkFlow;
    }
}

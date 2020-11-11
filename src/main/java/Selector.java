import java.util.HashMap;
import java.util.HashSet;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-11 23:19
 */
public interface Selector {

    void makeGraph(HashMap<Node, HashSet<Node>> graph);
    void findDependency(String change, HashMap<Node, HashSet<Node>> graph, HashSet<String> result, int flag, HashMap<Node, HashSet<Node>> testGraph);
    void init(boolean CM);
    void addScope(String path);
    void makeCallGraph();
}

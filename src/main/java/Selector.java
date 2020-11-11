import java.util.HashMap;
import java.util.HashSet;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-11 23:19
 */
public interface Selector {

    void FindDependency(HashMap<Node, HashSet<Node>> graph);
    void Selector(String change, HashMap<Node, HashSet<Node>> graph, HashSet<String> result, int flag, HashMap<Node, HashSet<Node>> testGraph);
    void init(boolean CM);
    void AddScope(String path);
    void MakeCallGraph();
}

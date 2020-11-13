import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.AnalysisScope;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.Entrypoint;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.File;
import java.util.*;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-11 23:22
 * 使用节点的后继进行图生成和测试用例选择
 */
public class CHAPredSelector implements Selector{

    AnalysisScope scope;
    public Boolean CM;
    CHACallGraph cg;


    /**
     * graph是记录依赖的数据结构，调用这个方法前需要根据目前的Scope初始化一次CHA
     * @param graph
     */
    @Override
    public void FindDependency(HashMap<Node, HashSet<Node>> graph) {
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                //只看和业务逻辑相关的代码，这里可以排除掉java/lang等一些自带的类库
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    Node left = new Node(node, CM);
                    if (!graph.containsKey(left)) graph.put(left, new HashSet<>());
                    //找到节点的所有后继
                    Iterator<CGNode> cgNodeIterator = cg.getPredNodes(node);
                    while (cgNodeIterator.hasNext()) {
                        CGNode dest = cgNodeIterator.next();
                        if (dest.getMethod() instanceof ShrikeBTMethod) {
                            //和上面的if语句同理
                            if ("Application".equals(dest.getMethod().getDeclaringClass().getClassLoader().toString())) {
                                Node right = new Node(dest,  CM);
                                //将新的边添加到图中
                                graph.get(left).add(right);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 真正的挑选用例的方法，Change是记录变化信息的，会根据具体的类和level，再加上传进去的Node来解析这个Node是否发生了变化，这样的好处是change的内部逻辑可以发生改变，但是不改变选择的代码
     * @param change ChangeInfo记录类
     * @param graph FindDependency生成的依赖图
     * @param result 结果
     * @param testGraph 测试依赖图
     */
    @Override
    public void Selector(ChangeInfo change, HashMap<Node, HashSet<Node>> graph, HashSet<String> result, HashMap<Node, HashSet<Node>> testGraph) {
        Queue<Node> queue = new LinkedList<>();
        for (Node key : graph.keySet()) {
            if(change.IsChange(key, CM)) queue.add(key);
        }
        HashSet<Node> vis = new HashSet<>();
        while (!queue.isEmpty()) {
            Node head = queue.poll();
            //防止圈的出现
            if (vis.contains(head)) {
                continue;
            }
            vis.add(head);
            if (graph.containsKey(head)) {
                queue.addAll(graph.get(head));
                for (Node node : graph.get(head)) {
                    //只关心存在于测试代码里面的方法和非初始化方法
                    if (testGraph.containsKey(node) && !node.WholeInfo().contains("<init>()V"))
                        result.add(node.WholeInfo());
                }
            }
        }
    }

    @Override
    public void init(boolean CM) {
        try{
            scope = AnalysisScopeReader.readJavaScope("scope.txt", new File("exclusion.txt"), ClassLoader.getSystemClassLoader());
            this.CM = CM;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 增加Scope
     * @param path
     */
    @Override
    public void AddScope(String path){
        try{
            scope.addClassFileToScope(ClassLoaderReference.Application, new File(path));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 生成CallGraph
     */
    @Override
    public void MakeCallGraph(){
        try{
            ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
            Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
            cg = new CHACallGraph(cha);
            cg.init(eps);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}

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
 * 使用节点的前继进行选择
 */
public class CHAPredSelector implements Selector{

    AnalysisScope scope;
    public Boolean CM;
    CHACallGraph cg;


    @Override
    public void FindDependency(HashMap<Node, HashSet<Node>> graph) {
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                //只看和业务逻辑相关的代码，这里可以排除掉java/lang等一些自带的类库
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String srcClassInnerName = method.getDeclaringClass().getName().toString();
                    String srcSignature = method.getSignature();
                    Node left = new Node(srcClassInnerName, srcSignature, CM);
                    if (!graph.containsKey(left)) graph.put(left, new HashSet<>());
                    //找到节点的所有后继
                    Iterator<CGNode> cgNodeIterator = cg.getPredNodes(node);
                    while (cgNodeIterator.hasNext()) {
                        CGNode dest = cgNodeIterator.next();
                        if (dest.getMethod() instanceof ShrikeBTMethod) {
                            //和上面的if语句同理
                            if ("Application".equals(dest.getMethod().getDeclaringClass().getClassLoader().toString())) {
                                String destClassInnerName = dest.getMethod().getDeclaringClass().getName().toString();
                                String destSignature = dest.getMethod().getSignature();
                                Node right = new Node(destClassInnerName, destSignature, CM);
                                //将新的边添加到图中
                                graph.get(left).add(right);
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void Selector(String change, HashMap<Node, HashSet<Node>> graph, HashSet<String> result, int flag, HashMap<Node, HashSet<Node>> testGraph) {
        Queue<Node> queue = new LinkedList<>();
        for (Node key : graph.keySet()) {
            if (flag == 1) {
                if (key.getClassInnerName().equals(change)) {
                    queue.add(key);
                }
            } else {
                if (key.getSignature().equals(change)) {
                    queue.add(key);
                }
            }
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

    public void AddScope(String path){
        try{
            scope.addClassFileToScope(ClassLoaderReference.Application, new File(path));
        }catch (Exception e){
            e.printStackTrace();
        }
    }

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

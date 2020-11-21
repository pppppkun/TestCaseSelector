# AutomatedTesting2020

选题：经典自动化测试

## 代码导读

算法释义，所用第三方库及版本，程序入口，程序结构……

### 程序结构

```
Project
├── pom.xml
└── src
    └── main
        ├── java
        │   ├── CHAPredSelector.java	# 利用CHA和Pred来选择测试用例
        │   ├── ChangeInfo.java				# 变更信息接口类
        │   ├── DotGenerator.java			# 操作Dot文件的工具类
        │   ├── FileUtils.java  			# 操作文件的工具类
        │   ├── Node.java							# 组合了CGNode的节点类
        │   ├── Selector.java					# 测试用例选择结构类
        │   ├── StrChangeInfo.java		# 利用字符串相等判断是否变更的变更信息类
        │   └── TestCaseSelect.java
        ├── resources
        │   ├── TailExclusion.txt			# 自己玩的
        │   ├── exclusion.txt
        │   ├── scope.txt
        │   └── wala.properties
        └── test
            └── SelectorTest.java			# 自己写的测试类
```

### 算法释义

```java
import java.util.HashMap;
import java.util.HashSet;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-11 23:19
 */
public interface Selector {

    void FindDependency(HashMap<Node, HashSet<Node>> graph);
    void Selector(ChangeInfo change, HashMap<Node, HashSet<Node>> graph, HashSet<String> result, HashMap<Node, HashSet<Node>> testGraph);
    void init(boolean CM);
    void AddScope(String path);
    void MakeCallGraph();
}
```

这个是Selector接口类，他的程序流是根据选择级别初始化`init`，根据要分析的文件添加`AddScope`，生成CallGraph`MakeCallGraph`，查找CallGraph里面的依赖`FindDependency`，根据依赖选择出测试用例`Selector`。抽象类的好处是我们可以搭配不同的CallGraph和查询依赖的方法，选择的方法也可以发生改变。我使用的是广度优先搜索来查找依赖，但是也可以使用递归或者深度优先，可拓展性非常强

```java
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
```

这里通过遍历cg的每一个node和对应节点的后继来生成整个图，因为Node重载了HashCode和Equals方法，所以可以直接添加到Set中，不会发生重复。

```java
    @Override
    public boolean equals(Object obj) {
        return WholeInfo().equals(((Node) obj).WholeInfo());
    }

    public String WholeInfo(){
        return ClassInnerName +  " " + Signature ;
    }

    @Override
    public int hashCode() {
        return WholeInfo().hashCode();
    }
```

```java
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
                if(CM){
                    for (Node node : graph.get(head)) {
                        //只关心存在于测试代码里面的方法和非初始化方法
                        if (testGraph.containsKey(node)){
                          // 类级别的选择只需要类相同就选出来
                            for(Node node1 : testGraph.keySet()){
                                if(node.getClassInnerName().equals(node1.getClassInnerName()) && node1.IsTest()){
                                    result.add(node1.WholeInfo());
                                }
                            }
                        }
                    }
                }
                else{
                    for (Node node : graph.get(head)) {
                        //只关心存在于测试代码里面的方法和非初始化方法
                        if (testGraph.containsKey(node) && node.IsTest())
                            result.add(node.WholeInfo());
                    }
                }
            }
        }
    }

```

这个Selector方法使用广度优先搜索来将变化的Node的信息传递到每一个依赖过这个Node的节点身上，所以不会出现漏报的现象。其中我们会判断Node是不是一个测试用例，这里的判断标准是是否打上了@Test注解

```java
    public boolean IsTest(){
        return cgNode.getMethod().getAnnotations().toString().contains("Test");
    }
```

其中我们将Node传递给Change来判断是否是变化的节点，这样Selector就不需要知道任何ChangeInfo的具体实现，可以让ChangeInfo自己选择判断是否变化的方法，比如字符串或者CGNode。ChangeInfo的接口如下

```java
    boolean IsChange(Node t, Boolean level);

    void AddChange(T t, Boolean CM);
```

StrChange的实现如下

```java
 @Override
    public boolean IsChange(Node s, Boolean CM) {
        return CM ? classChange.contains(s.getClassInnerName()) : methodChange.contains(s.getSignature());
    }

    @Override
    public void AddChange(String s, Boolean CM) {
        if(CM) classChange.add(s);
        else methodChange.add(s);
    }
```

### 程序入口

```java
public class TestCaseSelect {

    public static void main(String[] args) {
        HashSet<String> result = realEntry(args);
        assert result!=null;
        StringBuilder stringBuilder = new StringBuilder();
        for(String s : result){
            stringBuilder.append(s+"\n");
        }
        FileUtils.writeFile(args[0].equals("-c") ? "selection-class.txt" : "selection-method.txt", stringBuilder.toString());
    }
		// Real Entry
    public static HashSet<String> realEntry(String[] args) {

        Selector selector = new CHAPredSelector();

        switch (args[0]){
            case "-c":
                selector.init(true);
                break;
            case "-m":
                selector.init(false);

        }

        ArrayList<String> src = new ArrayList<>();
        ArrayList<String> test = new ArrayList<>();
        FileUtils.folderFind(args[1], src, test);

        for (String path : test) {
            selector.AddScope(path);
        }

        HashMap<Node, HashSet<Node>> testGraph = new HashMap<>();
        selector.MakeCallGraph();
        selector.FindDependency(testGraph);

        for (String path : src) {
            selector.AddScope(path);
        }
        HashMap<Node, HashSet<Node>> graph = new HashMap<>();
        selector.MakeCallGraph();
        selector.FindDependency(graph);
        StrChangeInfo changeInfo = new StrChangeInfo(args[2]);
        HashSet<String> result = new HashSet<>();
        selector.Selector(changeInfo, graph, result, testGraph);
        DotGenerator.GEN(graph, args[0].equals("-m")?1:0, "graph");
        return result;


    }

}
```

程序入口的程序流为新建一个CHAPredSelector，然后根据级别初始化它，然后用FileUtils找到源文件和测试文件的路径。接着我们添加测试文件的路径来找到一个测试依赖图，再添加源文件生成一个比较完整的依赖图。然后我们用StrChangeInfo来初始化ChangeInfo，最后我们会生成一个.dot文件和.txt文件。

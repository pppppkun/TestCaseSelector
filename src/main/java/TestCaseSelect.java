import com.ibm.wala.classLoader.*;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;

import java.io.*;
import java.util.*;

/**
 * @Author: pkun
 * @CreateTime: 2020-10-15 10:28
 */
public class TestCaseSelect {

    public static void main(String[] args) {
        realEntry(args);
    }

    public static HashSet<String> realEntry(String[] args) {
        try {
            //读取变更文件
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));
            String data = null;
            ArrayList<String> changeMethods = new ArrayList<>();
            while ((data = br.readLine()) != null) {
                changeMethods.add(data);
            }

            HashSet<String> result = new HashSet<>();
            switch (args[0]) {
                case "-m":
                    result = Model(args, changeMethods, 0);
                    break;
                case "-c":
                    result = Model(args, changeMethods, 1);
                    break;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 这个方法会对目标进行建模，并且生成.dot文件和被选择出的测试用例
     * @param args
     * @param changeMethods
     * @param flag
     * @return
     * @throws IOException
     * @throws InvalidClassFileException
     * @throws CancelException
     * @throws WalaException
     */
    private static HashSet<String> Model(String args[], ArrayList<String> changeMethods, int flag) throws IOException, InvalidClassFileException, CancelException, WalaException {
        ArrayList<String> src = new ArrayList<>();
        ArrayList<String> test = new ArrayList<>();
        folderFind(args[1], src, test);
        HashMap<Node, HashSet<Node>> graph = new HashMap<>();

        AnalysisScope scope = AnalysisScopeReader.readJavaScope("scope.txt", new File("exclusion.txt"), ClassLoader.getSystemClassLoader());

        for (String path : test) {
            scope.addClassFileToScope(ClassLoaderReference.Application, new File(path));
        }

        HashMap<Node, HashSet<Node>> testGraph = new HashMap<>();
        getGraph(scope, testGraph, flag);


        for (String path : src) {
            scope.addClassFileToScope(ClassLoaderReference.Application, new File(path));
        }

        getGraph(scope, graph, flag);

        HashSet<String> result = new HashSet<>();
        ArrayList<String> temp = new ArrayList<>(changeMethods);
        for (int i = 0; i < temp.size(); i++) {
            changeMethods.add(temp.get(i).split(" ")[1 - flag]);
            changeMethods.remove(0);
        }
        HashMap<String, HashSet<String>> dot = new HashMap<>();
        getDot(graph, dot, flag);

        for (String key : dot.keySet()) {
            for (String value : dot.get(key)) {
                System.out.println(key + " -> " + value);
            }
        }

        for(String s : changeMethods){
            findDependency(s, graph, result, flag, testGraph);
        }

        return result;
    }

    /**
     * 这个方法会从Scope中得到CallGraph，并且存到graph中
     * @param scope
     * @param graph
     * @param flag  flag == 0 -> Method     flag == 1 -> Class
     * @throws IOException
     * @throws InvalidClassFileException
     * @throws WalaException
     * @throws CancelException
     */

    private static void getGraph(AnalysisScope scope, HashMap<Node, HashSet<Node>> graph, int flag) throws IOException, InvalidClassFileException, WalaException, CancelException {
        //初始化
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
        CHACallGraph cg = new CHACallGraph(cha);
        cg.init(eps);
        boolean CM = flag == 1;
        //遍历整个图
        for (CGNode node : cg) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                //只看和业务逻辑相关的代码，这里可以排除掉java/lang等一些自带的类库
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String srcClassInnerName = method.getDeclaringClass().getName().toString();
                    String srcSignature = method.getSignature();
                    Node left = new Node(srcClassInnerName, srcSignature, CM);
                    if(!graph.containsKey(left)) graph.put(left, new HashSet<>());
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

    /**
     * 这个方法用来生成.dot文件
     * @param graph
     * @param Dot
     * @param flag  flag == 0 -> Method     flag == 1 -> Class
     */
    private static void getDot(HashMap<Node, HashSet<Node>> graph, HashMap<String, HashSet<String>> Dot, int flag) {
        for (Node n : graph.keySet()) {
            String var1 = flag == 1 ? n.getClassInnerName() : n.getSignature();
            if(!Dot.containsKey("\"" + var1 + "\"")) Dot.put("\"" + var1 + "\"", new HashSet<>());
            for (Node t : graph.get(n)) {
                String var2 = flag == 1 ? t.getClassInnerName() : t.getSignature();
                Dot.get("\"" + var1 + "\"").add("\"" + var2 + "\"");
            }
        }
    }

    /**
     * 这个方法会根据change_info来提取出测试代码和源代码之间的依赖，使用广度优先遍历
     * @param change 变化的语句
     * @param graph
     * @param result
     * @param flag
     * @param testGraph
     */
    private static void findDependency(String change, HashMap<Node, HashSet<Node>> graph, HashSet<String> result, int flag, HashMap<Node, HashSet<Node>> testGraph){
        Queue<Node> queue = new LinkedList<>();
        for(Node key : graph.keySet()){
            if(flag == 1){
                if(key.getClassInnerName().equals(change)) {
                    queue.add(key);
                }
            }else{
                 if(key.getSignature().equals(change)){
                    queue.add(key);
                }
            }
        }
        HashSet<Node> vis = new HashSet<>();
        while(!queue.isEmpty()){
            Node head = queue.poll();
            //防止圈的出现
            if(vis.contains(head)){
                continue;
            }
            vis.add(head);
            if(graph.containsKey(head)){
                queue.addAll(graph.get(head));
                for(Node node : graph.get(head)){
                    //只关心存在于测试代码里面的方法和非初始化方法
                    if(testGraph.containsKey(node) && !node.WholeInfo().contains("<init>()V")) result.add(node.WholeInfo());
                }
            }
        }
    }

    /**
     * 提取出源代码文件和测试代码文件的路径，存在src和test里面
     * @param path
     * @param src
     * @param test
     */
    private static void folderFind(String path, ArrayList<String> src, ArrayList<String> test) {
        File file = new File(path);
        if (file.exists()) {
            if (null == file.listFiles()) {
                return;
            }
            LinkedList<File> list = new LinkedList<>(Arrays.asList(file.listFiles()));
            File t = null;
            File s = null;
            for (File file1 : list) {
                if (file1.getName().equals("test-classes")) {
                    t = file1;
                }
                if (file1.getName().equals("classes")) {
                    s = file1;
                }
            }
            list = new LinkedList<>(Arrays.asList(t.listFiles()));
            addFilePath(list, test);
            list = new LinkedList<>(Arrays.asList(s.listFiles()));
            addFilePath(list, src);
        } else {
            System.out.println("文件不存在!");
        }
    }

    /**
     * 添加目录下面的所有文件到ArrayList中
     * @param list
     * @param arrayList
     */
    private static void addFilePath(LinkedList<File> list, ArrayList<String> arrayList) {
        while (!list.isEmpty()) {
            File[] files = list.removeFirst().listFiles();
            if (null == files) {
                continue;
            }
            for (File f : files) {
                if (f.isDirectory()) {
                    list.add(f);
                } else {
                    arrayList.add(f.getPath());
                }
            }
        }
    }

}

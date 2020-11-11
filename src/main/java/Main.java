import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-11 23:05
 */
public class Main {
    public static void main(String[] args) {

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

        //        for (int i = 0; i < temp.size(); i++) {
        //            changeMethods.add(temp.get(i).split(" ")[1 - flag]);
        //            changeMethods.remove(0);
        //        }

    }

    //        ArrayList<String> src = new ArrayList<>();
    //        ArrayList<String> test = new ArrayList<>();
    //        FileUtils.folderFind(args[1], src, test);
    //        HashMap<Node, HashSet<Node>> graph = new HashMap<>();
    //
    //        AnalysisScope scope = AnalysisScopeReader.readJavaScope("scope.txt", new File("exclusion.txt"), ClassLoader.getSystemClassLoader());
    //
    //        for (String path : test) {
    //            scope.addClassFileToScope(ClassLoaderReference.Application, new File(path));
    //        }
    //
    //        HashMap<Node, HashSet<Node>> testGraph = new HashMap<>();
    //        getGraph(scope, testGraph, flag);
    //
    //
    //        for (String path : src) {
    //            scope.addClassFileToScope(ClassLoaderReference.Application, new File(path));
    //        }
    //
    //        getGraph(scope, graph, flag);
    //
    //        HashSet<String> result = new HashSet<>();
    //        ArrayList<String> temp = new ArrayList<>(changeMethods);
    //        for (int i = 0; i < temp.size(); i++) {
    //            changeMethods.add(temp.get(i).split(" ")[1 - flag]);
    //            changeMethods.remove(0);
    //        }
    //        HashMap<String, HashSet<String>> dot = new HashMap<>();
    //        getDot(graph, dot, flag);
    //
    //        StringBuilder stringBuilder = new StringBuilder();
    //        stringBuilder.append("digraph g {\n");
    //
    //        for (String key : dot.keySet()) {
    //            for (String value : dot.get(key)) {
    //                stringBuilder.append("\t"+key + " -> " + value+";\n");
    //            }
    //        }
    //        stringBuilder.append("}");
    //        FileUtils.writeFile(args[0].equals("-c") ? "class-"+args[1].split("/")[1]+".dot":"method-"+args[1].split("/")[1]+".dot", stringBuilder.toString());
    //
    //        for (String s : changeMethods) {
    //            findDependency(s, graph, result, flag, testGraph);
    //        }
    //
    //        return result;
}

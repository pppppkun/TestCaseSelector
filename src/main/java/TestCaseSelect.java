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
        HashSet<String> result = realEntry(args);
        assert result!=null;
        StringBuilder stringBuilder = new StringBuilder();
        for(String s : result){
            stringBuilder.append(s+"\n");
        }
        FileUtils.writeFile(args[0].equals("-c") ? "selection-class.txt" : "selection-method.txt", stringBuilder.toString());
    }

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

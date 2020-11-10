import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.Language;
import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.dataflow.graph.IKilldallFramework;
import com.ibm.wala.ipa.callgraph.*;
import com.ibm.wala.ipa.callgraph.cha.CHACallGraph;
import com.ibm.wala.ipa.callgraph.impl.AllApplicationEntrypoints;
import com.ibm.wala.ipa.callgraph.impl.Util;
import com.ibm.wala.ipa.callgraph.propagation.SSAPropagationCallGraphBuilder;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.ipa.cha.ClassHierarchyFactory;
import com.ibm.wala.shrikeCT.InvalidClassFileException;
import com.ibm.wala.types.ClassLoaderReference;
import com.ibm.wala.util.CancelException;
import com.ibm.wala.util.WalaException;
import com.ibm.wala.util.config.AnalysisScopeReader;
import com.ibm.wala.util.graph.labeled.LabeledGraph;
import com.ibm.wala.util.graph.traverse.NumberedDFSDiscoverTimeIterator;
import com.ibm.wala.viz.DotUtil;

import java.io.*;
import java.util.*;

/**
 * @Author: pkun
 * @CreateTime: 2020-10-15 10:28
 */
public class TestCaseSelect {

    public static void main(String[] args) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));
            String data = null;
            ArrayList<String> changeMethods = new ArrayList<>();
            while ((data = br.readLine()) != null) {
                changeMethods.add(data);
            }
            HashSet<String> result = new HashSet<>();
            switch (args[0]) {
                case "-m":
                    result = MethodLevel(args, changeMethods, 0);
                    break;
                case "-c":
                    result = MethodLevel(args, changeMethods, 1);
                    break;
            }
            for (String s : result) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HashSet<String> TestMain(String[] args) {

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(args[2])));
            String data = null;
            ArrayList<String> changeMethods = new ArrayList<>();
            while ((data = br.readLine()) != null) {
                changeMethods.add(data);
            }
            HashSet<String> result = new HashSet<>();
            switch (args[0]) {
                case "-m":
                    result = MethodLevel(args, changeMethods, 0);
                    break;
                case "-c":
                    result = MethodLevel(args, changeMethods, 1);
                    break;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static HashSet<String> MethodLevel(String args[], ArrayList<String> changeMethods, int flag) throws IOException, InvalidClassFileException, CancelException, WalaException {
        ArrayList<String> src = new ArrayList<>();
        ArrayList<String> test = new ArrayList<>();
        folderFind(args[1], src, test);

        HashMap<String, HashSet<String>> testMethods = new HashMap<>();

        for (String path : test) {
            getMethodSignature(path, testMethods);
        }

        HashSet<String> result = new HashSet<>();
        if (flag == 1) {
            for (int i = 0; i < new ArrayList<>(changeMethods).size(); i++) {
                changeMethods.add(changeMethods.get(i).split(" ")[0]);
                changeMethods.remove(0);
            }
        }
        for (String change : changeMethods) {
            for (String key : testMethods.keySet()) {
                for (String value : testMethods.get(key)) {
                    if (value.contains(change)) {
                        result.add(key);
                    }
                }
            }
        }
        return result;
    }

    private static void getMethodSignature(String filePath, HashMap<String, HashSet<String>> methods) throws IOException, InvalidClassFileException, WalaException, CancelException {
        AnalysisScope scope = AnalysisScopeReader.readJavaScope("scope.txt", new File("exclusion.txt"), ClassLoader.getSystemClassLoader());
        scope.addClassFileToScope(ClassLoaderReference.Application, new File(filePath));
        ClassHierarchy cha = ClassHierarchyFactory.makeWithRoot(scope);
        Iterable<Entrypoint> eps = new AllApplicationEntrypoints(scope, cha);
        AnalysisOptions option = new AnalysisOptions(scope, eps);
        SSAPropagationCallGraphBuilder builder = Util.makeZeroCFABuilder(
                Language.JAVA, option, new AnalysisCacheImpl(), cha, scope);
        CHACallGraph cg = new CHACallGraph(cha);
        cg.init(eps);

//        System.out.println(DotUtil.dotOutput(builder.makeCallGraph(option), null, "Test").toString());
//        System.out.println(DotUtil.dotOutput(cg, null, "Test").toString());
//        String[] dots = DotUtil.dotOutput(cg, null, "Test").toString().split("\n");
//        for(int i = 0;i<dots.length;i++){
//            if(!dots[i].contains("java/lang")){
//                System.out.println(dots[i]);
//            }
//        }
        CallGraph cgNodes = builder.makeCallGraph(option);

        for (CGNode node : cgNodes) {
            if (node.getMethod() instanceof ShrikeBTMethod) {
                ShrikeBTMethod method = (ShrikeBTMethod) node.getMethod();
                if ("Application".equals(method.getDeclaringClass().getClassLoader().toString())) {
                    String classInnerName = method.getDeclaringClass().getName().toString();
                    String signature = method.getSignature();
                    HashSet<String> m = new HashSet<>();
                    for (CallSiteReference callSiteReference : method.getCallSites()) {
                        m.add(callSiteReference.getDeclaredTarget().getDeclaringClass().getName().toString() + " " + callSiteReference.getDeclaredTarget().getSignature());

                    }
                    methods.put(classInnerName + " " + signature, m);
                }
            }
        }
    }


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

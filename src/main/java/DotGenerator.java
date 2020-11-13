import java.util.HashMap;
import java.util.HashSet;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-11 23:06
 */
public class DotGenerator {

    public static void GEN(HashMap<Node, HashSet<Node>> graph, int flag, String fileName){
        HashMap<String, HashSet<String>> Dot = new HashMap<>();
        for (Node n : graph.keySet()) {
            String var1 = flag == 1 ? n.getClassInnerName() : n.getSignature();
            if (!Dot.containsKey("\"" + var1 + "\"")) Dot.put("\"" + var1 + "\"", new HashSet<>());
            for (Node t : graph.get(n)) {
                String var2 = flag == 1 ? t.getClassInnerName() : t.getSignature();
                Dot.get("\"" + var1 + "\"").add("\"" + var2 + "\"");
            }
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("digraph g {\n");
        for (String key : Dot.keySet()) {
            for (String value : Dot.get(key)) {
                stringBuilder.append("\t"+key + " -> " + value+";\n");
            }
        }
        stringBuilder.append("}");
        FileUtils.writeFile(fileName, stringBuilder.toString());
    }

}

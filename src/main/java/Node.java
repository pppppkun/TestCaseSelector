import com.ibm.wala.classLoader.ShrikeBTMethod;
import com.ibm.wala.ipa.callgraph.CGNode;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-11 12:41
 *
 * 相当于CGNode的代理类，使用ClassInnerName和签名联合判断两个节点是否相同
 */
public class Node {

    CGNode cgNode;
    String ClassInnerName;
    String Signature;
    // Class -> True
    // Method -> False;
    boolean CM;

    public Node(String classInnerName, String signature, boolean CM) {
        ClassInnerName = classInnerName;
        Signature = signature;
        this.CM = CM;
    }

    public Node(CGNode cgNode, boolean CM) {
        this.cgNode = cgNode;
        this.CM = CM;
        this.ClassInnerName = cgNode.getMethod().getDeclaringClass().getName().toString();
        this.Signature = cgNode.getMethod().getSignature();
    }

    public String getClassInnerName() {
        return ClassInnerName;
    }

    public String getSignature() {
        return Signature;
    }


    @Override
    public String toString() {
        if(CM){
            return "\"" + ClassInnerName + "\"";
        }else{
            return "\"" + Signature +"\"";
        }
    }

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
}

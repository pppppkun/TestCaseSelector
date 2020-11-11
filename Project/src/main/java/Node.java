/**
 * @Author: pkun
 * @CreateTime: 2020-11-11 12:41
 *
 * This class present a node in CHACallGraph
 */
public class Node {

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

    public boolean isCM() {
        return CM;
    }

    public void setCM(boolean CM) {
        this.CM = CM;
    }

    public String getClassInnerName() {
        return ClassInnerName;
    }

    public void setClassInnerName(String classInnerName) {
        ClassInnerName = classInnerName;
    }

    public String getSignature() {
        return Signature;
    }

    public void setSignature(String signature) {
        Signature = signature;
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
        if(obj instanceof Node)
            return this.WholeInfo().equals(((Node) obj).WholeInfo());
        else return super.equals(obj);
    }

    public String WholeInfo(){
        return ClassInnerName +  " " + Signature ;
    }

    @Override
    public int hashCode() {
        return WholeInfo().hashCode();
    }
}

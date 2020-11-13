import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-12 00:22
 */
public class StrChangeInfo implements ChangeInfo<String> {

    HashSet<String> classChange = new HashSet<>();
    HashSet<String> methodChange = new HashSet<>();


    public StrChangeInfo(String path) {
        ArrayList<String> changeMethods = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String data = null;
            while ((data = br.readLine()) != null) {
                changeMethods.add(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < changeMethods.size(); i++) {
            classChange.add(changeMethods.get(i).split(" ")[0]);
            methodChange.add(changeMethods.get(i).split(" ")[1]);
        }
    }

    @Override
    public boolean IsChange(Node s, Boolean CM) {
        return CM ? classChange.contains(s.getClassInnerName()) : methodChange.contains(s.getSignature());
    }

    @Override
    public void AddChange(String s, Boolean CM) {
        if(CM) classChange.add(s);
        else methodChange.add(s);
    }
}

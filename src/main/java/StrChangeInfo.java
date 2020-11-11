import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-12 00:22
 */
public class StrChangeInfo implements Iterable<String> {

    ArrayList<String> changeMethods = new ArrayList<>();


    public StrChangeInfo(String path){
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String data = null;
            while ((data = br.readLine()) != null) {
                changeMethods.add(data);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public Iterator<String> iterator() {
        return null;
    }

    @Override
    public void forEach(Consumer<? super String> action) {

    }

    @Override
    public Spliterator<String> spliterator() {
        return null;
    }
}

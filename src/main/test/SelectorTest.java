import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-09 21:28
 */
public class SelectorTest {

    private void testHelper(String path, HashSet<String> result) {
        try{
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
            String data = null;
            ArrayList<String> except = new ArrayList<>();
            while ((data = br.readLine()) != null) {
                except.add(data);
            }
            ArrayList<String> except_copy = new ArrayList<>(except);
            for(String s : except_copy){
                if(result.contains(s)){
                    result.remove(s);
                    except.remove(s);
                }
                else{
                    if(!s.equals("")){
                        System.out.println(s);
                        Assert.fail();
                    }
                    else{
                        except.remove(s);
                    }
                }
            }

            Assert.assertEquals(result.size(), except.size());

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Test
    public void TestCMD(){
        //-c Data/1-ALU/Target/ Data/1-ALU/data/change_info.txt
        String[] args = new String[]{"-m", "Data/0-CMD/Target/", "Data/0-CMD/data/change_info.txt"};
        HashSet<String> result = TestCaseSelect.realEntry(args);
        testHelper("Data/0-CMD/data/selection-method.txt", result);
        args[0] = "-c";
        result = TestCaseSelect.realEntry(args);
        testHelper("Data/0-CMD/data/selection-class.txt", result);
    }

    @Test
    public void TestALU(){
        //-c Data/1-ALU/Target/ Data/1-ALU/data/change_info.txt
        String[] args = new String[]{"-m", "Data/1-ALU/Target/", "Data/1-ALU/data/change_info.txt"};
        HashSet<String> result = TestCaseSelect.realEntry(args);
        testHelper("Data/1-ALU/data/selection-method.txt", result);
        args[0] = "-c";
        result = TestCaseSelect.realEntry(args);
        testHelper("Data/1-ALU/data/selection-class.txt", result);
    }

    @Test
    public void TestDataLog(){
        //-c Data/1-ALU/Target/ Data/1-ALU/data/change_info.txt
        String[] args = new String[]{"-m", "Data/2-DataLog/Target/", "Data/2-DataLog/data/change_info.txt"};
        HashSet<String> result = TestCaseSelect.realEntry(args);
        testHelper("Data/2-DataLog/data/selection-method.txt", result);
        args[0] = "-c";
        result = TestCaseSelect.realEntry(args);
        testHelper("Data/2-DataLog/data/selection-class.txt", result);
    }

    @Test
    public void TestBinaryHeap(){
        //-c Data/1-ALU/Target/ Data/1-ALU/data/change_info.txt
        String[] args = new String[]{"-m", "Data/3-BinaryHeap/Target/", "Data/3-BinaryHeap/data/change_info.txt"};
        HashSet<String> result = TestCaseSelect.realEntry(args);
        testHelper("Data/3-BinaryHeap/data/selection-method.txt", result);
        args[0] = "-c";
        result = TestCaseSelect.realEntry(args);
        testHelper("Data/3-BinaryHeap/data/selection-class.txt", result);
    }

    @Test
    public void TestNextDay(){
        //-c Data/1-ALU/Target/ Data/1-ALU/data/change_info.txt
        String[] args = new String[]{"-m", "Data/4-NextDay/Target/", "Data/4-NextDay/data/change_info.txt"};
        HashSet<String> result = TestCaseSelect.realEntry(args);
        testHelper("Data/4-NextDay/data/selection-method.txt", result);
        args[0] = "-c";
        result = TestCaseSelect.realEntry(args);
        testHelper("Data/4-NextDay/data/selection-class.txt", result);
    }

    @Test
    public void TestNextMoreTriangle(){
        //-c Data/1-ALU/Target/ Data/1-ALU/data/change_info.txt
        String[] args = new String[]{"-m", "Data/5-MoreTriangle/Target/", "Data/5-MoreTriangle/data/change_info.txt"};
        HashSet<String> result = TestCaseSelect.realEntry(args);
        testHelper("Data/5-MoreTriangle/data/selection-method.txt", result);
        args[0] = "-c";
        result = TestCaseSelect.realEntry(args);
        testHelper("Data/5-MoreTriangle/data/selection-class.txt", result);
    }

}

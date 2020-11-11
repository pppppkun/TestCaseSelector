import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * @Author: pkun
 * @CreateTime: 2020-11-11 23:18
 */
public class FileUtils {

    /**
     * 提取出源代码文件和测试代码文件的路径，存在src和test里面
     *
     * @param path
     * @param src
     * @param test
     */
    public static void folderFind(String path, ArrayList<String> src, ArrayList<String> test) {
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
     *
     * @param list
     * @param arrayList
     */
    public static void addFilePath(LinkedList<File> list, ArrayList<String> arrayList) {
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

    public static void writeFile(String fileName, String data) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
            out.write(data);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

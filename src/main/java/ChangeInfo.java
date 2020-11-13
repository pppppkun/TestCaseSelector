/**
 * @Author: pkun
 * @CreateTime: 2020-11-12 00:28
 */
public interface ChangeInfo<T> {

    boolean IsChange(Node t, Boolean level);

    void AddChange(T t, Boolean CM);

}

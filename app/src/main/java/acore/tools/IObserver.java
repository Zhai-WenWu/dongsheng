package acore.tools;

/**
 * Created by sll on 2017/8/30.
 */

public interface IObserver {
    public abstract void notify(String name, Object sender, Object data);
}

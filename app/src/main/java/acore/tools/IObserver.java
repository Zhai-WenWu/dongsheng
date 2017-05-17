package acore.tools;

/**
 * Created shiliangliang.
 */

public interface IObserver {
    public abstract void notify(String name, Object sender, Object data);
}

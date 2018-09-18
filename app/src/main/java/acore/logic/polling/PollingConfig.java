package acore.logic.polling;

public enum PollingConfig {

    COURSE_GUIDANCE(10000, 1);

    private long mMillis;
    private int mType;
    private IHandleMessage mIHandleMessage;

    private PollingConfig(long millis, int type) {
        this.mMillis = millis;
        this.mType = type;
    }

    public long getMillis() {
        return this.mMillis;
    }

    public int getType() {
        return this.mType;
    }

    public void registerIHandleMessage(IHandleMessage handleMessage) {
        this.mIHandleMessage = handleMessage;
    }

    public IHandleMessage getIHandleMessage() {
        return this.mIHandleMessage;
    }

    public void unregisterIHandleMessage() {
        this.mIHandleMessage = null;
    }
}

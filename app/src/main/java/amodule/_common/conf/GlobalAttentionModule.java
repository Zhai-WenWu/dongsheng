package amodule._common.conf;

public class GlobalAttentionModule {
    private String mAttentionUserCode;
    private boolean mAttention;
    private String mAttentionNum;

    public String getAttentionUserCode() {
        return mAttentionUserCode;
    }

    public void setAttentionUserCode(String attentionUserCode) {
        mAttentionUserCode = attentionUserCode;
    }

    public boolean isAttention() {
        return mAttention;
    }

    public void setAttention(boolean attention) {
        mAttention = attention;
    }

    public String getAttentionNum() {
        return mAttentionNum;
    }

    public void setAttentionNum(String attentionNum) {
        mAttentionNum = attentionNum;
    }
}

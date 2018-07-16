package third.aliyun.edit.msg.body;


import third.aliyun.edit.effects.control.EffectInfo;

public class SelectColorFilter {
    private EffectInfo mEffectInfo;

    private int mIndex;

    private SelectColorFilter(Builder builder) {
        mEffectInfo = builder.mEffectInfo;
        mIndex = builder.mIndex;
    }


    public static final class Builder {
        private EffectInfo mEffectInfo;
        private int mIndex;

        public Builder() {
        }

        public Builder effectInfo(EffectInfo val) {
            mEffectInfo = val;
            return this;
        }

        public Builder index(int val) {
            mIndex = val;
            return this;
        }

        public SelectColorFilter build() {
            return new SelectColorFilter(this);
        }
    }

    public EffectInfo getEffectInfo() {
        return mEffectInfo;
    }
}

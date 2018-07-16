/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package third.aliyun.edit.effects.control;
/**
 * NOTE: item order must match
 */
public enum UIEditorPage {
    FILTER_EFFECT,
    AUDIO_MIX,
    COVER_IMG;

    public static UIEditorPage get(int index) {
        return values()[index];
    }

    public int index() {
        return ordinal();
    }
}

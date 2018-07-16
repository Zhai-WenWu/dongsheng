/*
 * Copyright (C) 2010-2017 Alibaba Group Holding Limited.
 */

package third.aliyun.widget;

import android.view.View;


public interface SizeChangedNotifier {

    public interface Listener {
        void onSizeChanged(View view, int w, int h, int oldw, int oldh);
    }

    void setOnSizeChangedListener(Listener listener);

}

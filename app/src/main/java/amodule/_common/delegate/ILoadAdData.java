package amodule._common.delegate;

import android.app.Activity;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Map;

import third.ad.scrollerAd.XHAllAdControl;

/**
 * Date:2018/3/14.
 * Desc:
 * Author:SLL
 * Email:
 */

public interface ILoadAdData {
    void loadAdData(@NonNull ArrayList<String> listIds, @NonNull XHAllAdControl.XHBackIdsDataCallBack xhBackIdsDataCallBack,
                    @NonNull Activity act, String StatisticKey);
}

package third.ad.option;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.StringManager;
import third.ad.scrollerAd.XHScrollerAdParent;

/**
 *条件拼装数据
 */
public abstract class  AdOptionList extends AdOptionParent {

    public AdOptionList(String[] adPlayIds) {
        super(adPlayIds);
    }
}
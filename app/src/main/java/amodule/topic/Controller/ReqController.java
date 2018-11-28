package amodule.topic.Controller;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.Map;

import acore.logic.AppCommon;
import acore.logic.LoginManager;
import acore.logic.XHClick;
import acore.tools.StringManager;
import amodule._common.conf.GlobalAttentionModule;
import amodule._common.conf.GlobalVariableConfig;
import amodule.dish.activity.ShortVideoDetailActivity;
import amodule.topic.activity.SearchTopicActivity;
import amodule.topic.activity.TopicInfoActivity;
import amodule.user.activity.login.LoginByAccout;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;
import aplug.basic.ReqInternet;

public class ReqController {

    ReqView mView;

    public ReqController(ReqView view) {
        this.mView = view;
    }

    public void getTopicRecom() {
        ReqEncyptInternet.in().doGetEncypt(StringManager.API_TOPIC_RECOM, "source=" + 2, new InternetCallback() {

            private List<Map<String, String>> mInfoMap;

            @Override
            public void loaded(int i, String s, Object o) {

                if (i >= ReqInternet.REQ_OK_STRING) {
                    mInfoMap = StringManager.getListMapByJson(o);
//                    StringManager.getFirstMap(mInfoMap.get(0).get("author")).get("code");
                } else {
                    mInfoMap = null;
                }
                mView.upData(s, mInfoMap);
            }
        });
    }

    public void getTopicSearch(String keywords, int page) {
        ReqEncyptInternet.in().doGetEncypt(StringManager.API_TOPIC_SEARCH, "keywords=" + keywords + "&page" + page, new InternetCallback() {

            private List<Map<String, String>> mInfoMap;

            @Override
            public void loaded(int i, String s, Object o) {

                if (i >= ReqInternet.REQ_OK_STRING) {
                    mInfoMap = StringManager.getListMapByJson(o);
                } else {
                    mInfoMap = null;
                }
                mView.upData(s, mInfoMap);
            }
        });
    }
}

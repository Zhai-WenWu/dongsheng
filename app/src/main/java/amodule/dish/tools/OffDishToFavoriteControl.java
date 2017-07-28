package amodule.dish.tools;

import android.app.Activity;
import android.content.Context;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.Map;

import acore.override.activity.base.BaseActivity;
import acore.override.activity.mian.MainBaseActivity;
import acore.tools.FileManager;
import acore.tools.StringManager;
import amodule.dish.db.DishOffData;
import amodule.dish.db.DishOffSqlite;
import amodule.dish.db.ShowBuySqlite;
import amodule.dish.view.DishsWebView;
import amodule.main.Main;
import aplug.basic.InternetCallback;
import aplug.basic.ReqEncyptInternet;

/**
 * Created by Fang Ruijiao on 2017/7/25.
 */
public class OffDishToFavoriteControl {

    public static void setIsAutoOffDish(Context context,boolean isAutoOffDish){
        FileManager.saveShared(context,FileManager.xmlFile_appInfo,"isAutoOffDish",isAutoOffDish ? "2" : "1");
    }

    public static boolean getIsAutoOffDish(Context context){
        Object isAutoOffDish = FileManager.loadShared(context,FileManager.xmlFile_appInfo,"isAutoOffDish");
        return "2".equals(String.valueOf(isAutoOffDish));
    }

    public static synchronized void offDishToFavorite(final Activity context) {
        try {
            if(context == null) return;
            RelativeLayout rootLayout = null;
            if (context instanceof BaseActivity) {
                rootLayout = ((BaseActivity) context).rl;
            } else if (context instanceof MainBaseActivity) {
                rootLayout = ((MainBaseActivity) context).rl;
            } else if (context instanceof Main) {
                rootLayout = ((Main) context).getRootLayout();
            }
            if (rootLayout == null) return;

            final RelativeLayout mRootLayout = rootLayout;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final ShowBuySqlite oldDishSqlite = new ShowBuySqlite(context);
                    int oldDishCount = oldDishSqlite.selectCount();
                    if (oldDishCount <= 0) return;
                    final ArrayList<String> codes = oldDishSqlite.getAllCodes();
                    if(codes.size() <= 0) return;
                    final ArrayList<DishOffData> oldDishArray = oldDishSqlite.getAllDataFromDB();
                    if(oldDishArray.size() <= 0)return;
                    final DishOffSqlite dishOffSqlite = new DishOffSqlite(context);
                    mRootLayout.post(new Runnable() {
                        @Override
                        public void run() {
                            addCollection(context, codes);
                            //同步cookie并获得webview
                            final DishsWebView webView = new DishsWebView(context);
                            webView.setOnLoadCallback(new DishsWebView.OnWebViewLoadDataCallback() {
                                int index = 0;
                                @Override
                                public void onLoadFinish() {
                                    DishOffData dishOffData = oldDishArray.get(index);
                                    ArrayList<Map<String,String>> arrayList = StringManager.getListMapByJson(dishOffData.getJson());
                                    if(arrayList.size() > 0) {
                                        Map<String, String> offDishMap = arrayList.get(0);
                                        offDishMap.put("type", offDishMap.get("hasVideo"));
                                        offDishMap.put("isMakeImg", "2");
                                        offDishMap.put("isFine", offDishMap.get("rank"));
                                        dishOffData.setJson(StringManager.getJsonByMap(offDishMap).toString());
                                        dishOffSqlite.insert(oldDishArray.get(index));
                                        oldDishSqlite.deleteByCode(codes.get(index));
                                    }
                                    if (index + 1 < codes.size()) {
                                        webView.loadDishData(codes.get(++index));
                                    }
                                }
                            });
                            mRootLayout.addView(webView, 0, 0);
                            webView.loadDishData(codes.get(0));
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addCollection(Context context, ArrayList<String> codes) {
        StringBuffer params = new StringBuffer("codes=");
        for (String code : codes) {
            params.append(code);
            params.append(",");
        }
        ReqEncyptInternet.in().doEncypt(StringManager.api_addCollection, params.toString(), new InternetCallback(context) {
            @Override
            public void loaded(int i, String s, Object o) {

            }
        });
    }


}

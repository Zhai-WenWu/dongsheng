package aplug.basic;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import acore.tools.StringManager;
import xh.basic.tool.UtilString;

/**
 * 上传管理控制类
 * 单例模式
 *
 */
public class BreakPointUploadManager {

    public static String TYPE_IMG="img";//img类型
    public static String TYPE_VIDEO="video";//video类型
    private Timer timer;//定时控制----------要释放
    private static BreakPointUploadManager breakPointUploadManager;

    private HashMap<String,BreakPointControl> mapControls= new HashMap<>();
    private HashMap<String,BreakPointTokenBean> tokenBeans= new HashMap<>();

    public static BreakPointUploadManager getInstance(){
        if(breakPointUploadManager==null){
            synchronized (BreakPointUploadManager.class){
                breakPointUploadManager= new BreakPointUploadManager();
            }
        }
        return breakPointUploadManager;
    }
    public BreakPointUploadManager (){
    }

    /**
     * 添加数据集合
     * @param filePath
     * @param breakPointContorl
     */
    public void addBreakPointContorl(String filePath,BreakPointControl breakPointContorl){
        mapControls.put(filePath,breakPointContorl);

        if(mapControls.size()==1){//数据为1时要及时开启
            autoSaveData();
        }
    }
    public void ReqToken(Context context, final String type){

        if(tokenBeans.get(type).isReqstate())return;
        tokenBeans.get(type).setReqstate(true);
        String params= "type="+type;
        ReqInternet.in().doPost(StringManager.api_getQiniuToken,params, new InternetCallback(context) {
            @Override
            public void loaded(int i, String s, Object o) {
                if(i>=ReqInternet.REQ_OK_STRING){
                    ArrayList<Map<String,String>> listmaps= UtilString.getListMapByJson(o);
                    BreakPointTokenBean breakPointTokenBean= tokenBeans.get(type);
                    breakPointTokenBean.setKey(listmaps.get(0).get("keyPrefix"));
                    breakPointTokenBean.setToken(listmaps.get(0).get("token"));
                    breakPointTokenBean.setDomian(listmaps.get(0).get("domain"));
                    if(!TextUtils.isEmpty(listmaps.get(0).get("expired"))){
                        breakPointTokenBean.setExpiredTime(Integer.parseInt(listmaps.get(0).get("expired")));
                        breakPointTokenBean.setStartTime(System.currentTimeMillis());
                    }
                    reStartBreakPoint(type);
                }else{
                }
                tokenBeans.get(type).setReqstate(false);
            }
        });
    }
    /**
     * 获取当前token 是否过期状态
     */
    public boolean getTokenState(String type){
       if(!tokenBeans.containsKey(type)){
           Log.i("qiniu", "不包含数据");
            BreakPointTokenBean breakPointTokenBean= new BreakPointTokenBean(type);
            tokenBeans.put(type,breakPointTokenBean);
           return breakPointTokenBean.getTokenState();
       }else{
            return tokenBeans.get(type).getTokenState();
       }

    }
    /**
     * 每2秒自动保存数据
     * 开启：当集合数据中有一个数据开启
     * 结束：当前集合无数据时关闭
     */
    private void autoSaveData(){
        if(timer==null)
            timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (BreakPointUploadManager.this){
                    for(String key: mapControls.keySet()){
                        mapControls.get(key).saveData();
                    }
                }
            }
        },3000,3000);
    }
    /**
     * 获取当前的key
     */
    public String getKey(String type){
        if(!tokenBeans.containsKey(type))
            return "";
        String key= tokenBeans.get(type).getKey();
        return key;
    }

    /**
     * 获取当前token
     * @return
     */
    public String getToken(String type){
        if(!tokenBeans.containsKey(type))
            return "";
        return tokenBeans.get(type).getToken();
    }

    /**
     * 获取当前的domain
     * @param type
     */
    public String getDomain(String type){
        if(!tokenBeans.containsKey(type))
            return "";
        return tokenBeans.get(type).getDomian();
    }

    /**
     * 开启全部因为过期暂停的上传
     */
    private synchronized void reStartBreakPoint(String type){
        for(String key : mapControls.keySet()){
            mapControls.get(key).startReCallback(type);
        }
    }

    /**
     * 删除对应的数据
     * @param filePath
     */
    public synchronized void delBreakPointUpload(String filePath){
        if(mapControls.size()>0&&mapControls.containsKey(filePath)) {
            mapControls.get(filePath).delData();
            mapControls.remove(filePath);
            if (mapControls.size() <= 0) {//当前数据已删除完毕，清除定时器
                timer.cancel();
                timer = null;
            }
        }
    }
}

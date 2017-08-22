package aplug.web.tools;

import android.provider.Settings;
import android.text.TextUtils;

/**
 * 香哈模版更新
 */

public class XHTemplateManager {
    public static long starttime;
    private String[] templates=new String[]{"","",""};
    private int templateNum=-1;
    private TemplateWebViewControl templateWebViewControl;
    public XHTemplateManager(){
        starttime= System.currentTimeMillis();
        templateNum=0;
    }

    /**
     * 检查全部更新
     */
    public void CheckUpdataAllTemplate(){
        if(templateNum<=0||templateNum>=templates.length) {
            templateNum = 0;
        }
        String requestMethod=templates[templateNum];
        if(TextUtils.isEmpty(requestMethod)){
            ++templateNum;
            CheckUpdataAllTemplate();
        }else{
            getSingleTemplate(requestMethod, new TemplateWebViewControl.MouldCallBack() {
                @Override
                public void load(boolean isSuccess, String data, String requestMothed, String version) {
                    ++templateNum;
                    CheckUpdataAllTemplate();
                }
            });
        }
    }

    /**
     * 获取单个模版
     * @param requestMethod
     * @param mouldCallBack
     */
    public void getSingleTemplate(String requestMethod,TemplateWebViewControl.MouldCallBack mouldCallBack){
        if(templateWebViewControl==null){
            templateWebViewControl= new TemplateWebViewControl();
        }
        templateWebViewControl.setMouldCallBack(mouldCallBack);
        templateWebViewControl.getH5MDWithRequestMed(requestMethod);
    }
}

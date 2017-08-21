package aplug.web.tools;

import android.text.TextUtils;

/**
 * 香哈模版更新
 */

public class XHTemplateManager {

    private String[] templates=new String[]{"","",""};
    private int templateNum=-1;
    private TemplateWebViewControl templateWebViewControl;

    public void CheckUpdataAllTemplate(){
        if(templateNum<=0||templateNum>=templates.length) {
            templateNum = 0;
        }
        if(templateWebViewControl==null){
            templateWebViewControl= new TemplateWebViewControl();
        }
        templateWebViewControl.setMouldCallBack(new TemplateWebViewControl.MouldCallBack() {
            @Override
            public void load(boolean isSuccess, String data, String requestMothed, String version) {
                ++templateNum;
                CheckUpdataAllTemplate();
            }
        });
        if(TextUtils.isEmpty(templates[templateNum])){
            ++templateNum;
            CheckUpdataAllTemplate();
        }else
        templateWebViewControl.getH5MDWithRequestMed(templates[templateNum]);
    }

}

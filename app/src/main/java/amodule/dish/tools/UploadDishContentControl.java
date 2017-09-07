package amodule.dish.tools;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.Toast;

import com.xiangha.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import acore.tools.Tools;
import acore.widget.EditTextNoEmoji;

/**
 * Created by XiangHa on 2016/10/25.
 */

public class UploadDishContentControl {

    /**禁止输入特殊符号*/
    public static void addDishNameChangeListener(final Activity act, final EditText dishNameEt) {
        dishNameEt.addTextChangedListener(new TextWatcher() {
            boolean resetText = false;
            String inputBeforText;
            int oldSelectIndex = 0;
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if(count == 0 && !resetText){
                    inputBeforText = dishNameEt.getText().toString();
                    oldSelectIndex = start;
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (before == 0 && !resetText){
                    String addStr = s.toString().substring(start, start + count);
                    String removeAfterStr = getRemove(act,addStr.toString());
                    int removeAfterCount = removeAfterStr.length();
                    if (removeAfterCount >= 2 && EditTextNoEmoji.containsEmoji(removeAfterStr)) {//表情符号的字符长度最小为2
                        resetText = true;
                        Toast.makeText(act, "不支持输入Emoji表情符号", Toast.LENGTH_SHORT).show();
                        //是表情符号就将文本还原为输入表情符号之前的内容
                        dishNameEt.setText(inputBeforText);
                        dishNameEt.setSelection(oldSelectIndex);
                    }else{
                        resetText = true;
                        inputBeforText = inputBeforText.substring(0,start) + removeAfterStr + inputBeforText.substring(start);
                        dishNameEt.setText(inputBeforText);
                        dishNameEt.setSelection(oldSelectIndex + removeAfterCount);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!resetText){
                    int maxLength = -1;
                    String hint = "";
                    if(dishNameEt.getTag(R.id.dish_upload_number) != null){
                        maxLength = Integer.parseInt(String.valueOf(dishNameEt.getTag(R.id.dish_upload_number)));
                        hint = String.valueOf(dishNameEt.getTag(R.id.dish_upload_hint));
                    }
                    if(maxLength > 0 && s.toString().length() > maxLength){
                        resetText = true;
                        dishNameEt.setText(inputBeforText.subSequence(0, maxLength));
                        dishNameEt.setSelection(maxLength);
                        Tools.showToast(act, hint);
                    }
                }else{
                    resetText = false;
                }
            }
        });
    }

    private static String getRemove(Activity act,String str){
        String digits = "·！@￥%&*，。、；‘【】-=《》？：“”｛｝|、,./;'[]\\{}|:\">?<+_—*&^$!~`";
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < str.length(); i++) {
            if (digits.indexOf(str.charAt(i)) < 0) {
                sb.append(str.charAt(i));
            }else{
                Toast.makeText(act, "不支持输入字符", Toast.LENGTH_SHORT).show();
            }
        }
        return sb.toString();
    }

    public static String getDishMakeData(ArrayList<Map<String,String>> mArrayList){
        JSONArray jsonArray = new JSONArray();
        int setpIndex = 1;
        try {
            for (Map<String, String> map : mArrayList) {
                String makeInfo = map.get("makesInfo");
                String videoInfo = map.get("videoInfo");
                // 如果没有步骤图并且没有图片则舍弃该数据;
                if (makeInfo.length() == 0 && videoInfo.length() == 0) {
                    continue;
                }
                // 数据库存储使用Json格式
                JSONObject jsonObj = new JSONObject();
                jsonObj.put("makesStep", setpIndex);
                jsonObj.put("videoInfo", videoInfo);
                jsonObj.put("videoTime", map.get("videoTime"));
                jsonObj.put("makesInfo", makeInfo);
                jsonObj.put("makesImg", map.get("makesImg"));
                jsonArray.put(jsonObj);
                setpIndex++;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return jsonArray.toString();
    }
}

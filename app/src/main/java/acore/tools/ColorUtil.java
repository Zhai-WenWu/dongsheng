package acore.tools;

import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;

/**
 * Description :
 * PackageName : acore.tools
 * Created by mrtrying on 2018/11/13 10:54.
 * e_mail : ztanzeyu@gmail.com
 */
public class ColorUtil {

    static final String TAG = "ColorUtil";

    public static int parseColor(String colorString) {
        return parseColor(colorString,Color.TRANSPARENT);
    }

    /**
     * <ul>
     * <li><code>#RGB</code></li>
     * <li><code>#RRGGBB</code></li>
     * <li><code>#ARGB</code></li>
     * <li><code>#AARRGGBB</code></li>
     * </ul>
     *
     * @param colorString
     *
     * @return
     */
    public static int parseColor(String colorString,int defaultColor) {
        int resultColor = defaultColor;
        if(!TextUtils.isEmpty(colorString) && colorString.startsWith("#")){
            String colorSuffix = colorString.substring(1);
            if(colorSuffix.length() == 3 || colorSuffix.length() == 4){
                String tempColorString = "#";
                for(int i =0;i<colorSuffix.length();i++){
                    char c = colorSuffix.charAt(i);
                    tempColorString += c;
                    tempColorString += c;
                }
                Log.i(TAG, "parseColor: " + tempColorString);
                resultColor = Color.parseColor(tempColorString);
            }else if(colorSuffix.length() == 6 || colorSuffix.length() == 8){
                resultColor = Color.parseColor(colorString);
            }
        }
        return resultColor;
    }
}

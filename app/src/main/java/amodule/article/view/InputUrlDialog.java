package amodule.article.view;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xiangha.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PackageName : amodule.article.view
 * Created by MrTrying on 2017/5/19 09:46.
 * E_mail : ztanzeyu@gmail.com
 */

public class InputUrlDialog extends Dialog implements View.OnClickListener {

    private Pattern WEB_URL;


    private void initWebUrPatternl() {
        // all domain names
        String[] ext = {
                "top", "com", "net", "org", "edu", "gov", "int", "mil", "cn", "tel", "biz", "cc", "tv", "info", "name", "hk", "mobi", "asia", "cd", "travel", "pro", "museum", "coop", "aero", "ad", "ae", "af",
                "ag", "ai", "al", "am", "an", "ao", "aq", "ar", "as", "at", "au", "aw", "az", "ba", "bb", "bd", "be", "bf", "bg", "bh", "bi", "bj", "bm", "bn", "bo", "br", "bs", "bt", "bv", "bw", "by", "bz",
                "ca", "cc", "cf", "cg", "ch", "ci", "ck", "cl", "cm", "cn", "co", "cq", "cr", "cu", "cv", "cx", "cy", "cz", "de", "dj", "dk", "dm", "do", "dz", "ec", "ee", "eg", "eh", "es", "et", "ev", "fi",
                "fj", "fk", "fm", "fo", "fr", "ga", "gb", "gd", "ge", "gf", "gh", "gi", "gl", "gm", "gn", "gp", "gr", "gt", "gu", "gw", "gy", "hk", "hm", "hn", "hr", "ht", "hu", "id", "ie", "il", "in", "io",
                "iq", "ir", "is", "it", "jm", "jo", "jp", "ke", "kg", "kh", "ki", "km", "kn", "kp", "kr", "kw", "ky", "kz", "la", "lb", "lc", "li", "lk", "lr", "ls", "lt", "lu", "lv", "ly", "ma", "mc", "md",
                "mg", "mh", "ml", "mm", "mn", "mo", "mp", "mq", "mr", "ms", "mt", "mv", "mw", "mx", "my", "mz", "na", "nc", "ne", "nf", "ng", "ni", "nl", "no", "np", "nr", "nt", "nu", "nz", "om", "qa", "pa",
                "pe", "pf", "pg", "ph", "pk", "pl", "pm", "pn", "pr", "pt", "pw", "py", "re", "ro", "ru", "rw", "sa", "sb", "sc", "sd", "se", "sg", "sh", "si", "sj", "sk", "sl", "sm", "sn", "so", "sr", "st",
                "su", "sy", "sz", "tc", "td", "tf", "tg", "th", "tj", "tk", "tm", "tn", "to", "tp", "tr", "tt", "tv", "tw", "tz", "ua", "ug", "uk", "us", "uy", "va", "vc", "ve", "vg", "vn", "vu", "wf", "ws",
                "ye", "yu", "za", "zm", "zr", "zw"
        };
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        for (int i = 0; i < ext.length; i++) {
            sb.append(ext[i]);
            sb.append("|");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        // final pattern str
        String pattern = "((https?|s?ftp|irc[6s]?|git|afp|telnet|smb)://)?((\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3})|((www\\.|[a-zA-Z\\.]+\\.)?[a-zA-Z0-9\\-]+\\." + sb.toString() + "(:[0-9]{1,5})?))((/[a-zA-Z0-9\\./,;\\?'\\+&%\\$#=~_\\-]*)|([^\\u4e00-\\u9fa5\\s0-9a-zA-Z\\./,;\\?'\\+&%\\$#=~_\\-]*))";
        // Log.v(TAG, "pattern = " + pattern);
        WEB_URL = Pattern.compile(pattern);
    }

    private EditText urlEdit;
    private EditText descEdit;

    private OnReturnResultCallback mOnReturnResultCallback;

    public InputUrlDialog(@NonNull Context context) {
        super(context, R.style.dialog);
        setContentView(R.layout.a_article_view_link);
        urlEdit = (EditText) findViewById(R.id.url_edit);
        descEdit = (EditText) findViewById(R.id.desc_edit);

        findViewById(R.id.sure).setOnClickListener(this);
        findViewById(R.id.cannel).setOnClickListener(this);
        findViewById(R.id.dialog_root).setOnClickListener(this);
        //
        initWebUrPatternl();
    }

    public void setDescDefault(String desc) {
        if (!TextUtils.isEmpty(desc)) {
            descEdit.setText(desc);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cannel:
                if (mOnReturnResultCallback != null) {
                    mOnReturnResultCallback.onCannel();
                }
            case R.id.dialog_root:
                dismiss();
                break;
            case R.id.sure:
                String url = urlEdit.getText().toString().trim();
                Matcher htmlUrl = WEB_URL.matcher(url);
                //正则匹配
                if (htmlUrl.matches()) {
                    dismiss();
                    String desc = descEdit.getText().toString().trim();
                    if (mOnReturnResultCallback != null) {
                        mOnReturnResultCallback.onSure(url, desc);
                    }
                } else {
                    Toast.makeText(getContext(), "请输入正确链接地址", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public interface OnReturnResultCallback {
        public void onSure(String url, String desc);

        public void onCannel();
    }

    public OnReturnResultCallback getOnReturnResultCallback() {
        return mOnReturnResultCallback;
    }

    public void setOnReturnResultCallback(OnReturnResultCallback mOnReturnResultCallback) {
        this.mOnReturnResultCallback = mOnReturnResultCallback;
    }
}

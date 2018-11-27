package amodule.other.activity;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import com.xiangha.R;

import acore.logic.AppCommon;
import acore.logic.SpecialOrder;

public class ClientDebug extends AppCompatActivity implements View.OnClickListener {

    private EditText mInputEdit,mInputEdit2;
    private Switch mStatShowBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_debug);
        initUI();
    }

    private void initUI() {
        initTopbar();

        mStatShowBtn = findViewById(R.id.stat_show_switch);
        //统计数据提示框
        mStatShowBtn.setChecked(SpecialOrder.isOpenSwitchStatLayout(this));
        mStatShowBtn.setOnCheckedChangeListener((buttonView, isChecked) -> SpecialOrder.switchStatLayoutVisibility(ClientDebug.this));

        mInputEdit = findViewById(R.id.edit_text);
        mInputEdit2 = findViewById(R.id.edit_text2);

        findViewById(R.id.goto_btn).setOnClickListener(this);
        findViewById(R.id.goto_btn2).setOnClickListener(this);
    }

    private void initTopbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    @Override
    public void onClick(View v) {
        String inputStr = null;
        switch (v.getId()) {
            case R.id.goto_btn:
                inputStr = mInputEdit.getText().toString();
                if (TextUtils.isEmpty(inputStr)) {
                    Toast.makeText(ClientDebug.this, "输入内容为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!inputStr.startsWith(AppCommon.XH_PROTOCOL))
                    inputStr = AppCommon.XH_PROTOCOL + inputStr;
                break;
            case R.id.goto_btn2:
                inputStr = mInputEdit2.getText().toString();
                if (TextUtils.isEmpty(inputStr)) {
                    Toast.makeText(ClientDebug.this, "输入内容为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                final String fixedStr = "UIWebView.app?url=";
                if (!inputStr.startsWith(fixedStr))
                    inputStr = fixedStr + Uri.encode(inputStr);
                else {
                    inputStr = fixedStr + Uri.encode(inputStr.substring(fixedStr.length()));
                }
                break;
        }
        AppCommon.openUrl(ClientDebug.this, inputStr, true);
    }
}

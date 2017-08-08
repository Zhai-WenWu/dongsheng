package amodule.main.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.xianghatest.R;

import acore.override.activity.base.BaseActivity;
import amodule.user.activity.MyFavorite;

/**
 * Created by Fang Ruijiao on 2017/8/1.
 */
public class HintMyselfDialog extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a_myself_hint);
        findViewById(R.id.a_hint_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HintMyselfDialog.this, MyFavorite.class);
                startActivity(intent);
                HintMyselfDialog.this.finish();
            }
        });
    }
}

package amodule.user.activity.login;

import android.os.Bundle;

/**
 * Created by ：fei_teng on 2017/3/14 17:14.
 */

public class BindePoneFromWeb extends LostSecret {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void initData() {
        super.initData();
        origin = ORIGIN_BIND_FROM_WEB;
    }
}

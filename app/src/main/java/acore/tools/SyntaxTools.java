package acore.tools;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.ArrayList;

/**
 * Created by ：fei_teng on 2017/3/14 11:38.
 */

public class SyntaxTools {

    public static void loop(@NonNull ArrayList list, @NonNull LooperCallBack callBack) {
        for (int i = 0; i < list.size(); i++) {
            if (callBack.loop(i, list.get(i))) {
                break;
            }
        }
    }

    public static void runOnUiThread(@NonNull Runnable runnable) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
    }

    public interface LooperCallBack {

        boolean loop(int i, Object object);
    }
}

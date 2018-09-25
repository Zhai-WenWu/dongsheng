package acore.logic.stat.intefaces;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

/**
 * Description :
 * PackageName : acore.interfaces
 * Created by tanzeyu on 2018/6/14 21:33.
 * e_mail : ztanzeyu@gmail.com
 */
public interface OnItemClickListenerStatCallback {
    void onItemClicked(AdapterView<?> parent, View view, int position, long id);
}

package amodule.article.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Map;

/**
 * PackageName : amodule.article.adapter
 * Created by MrTrying on 2017/6/19 10:56.
 * E_mail : ztanzeyu@gmail.com
 */

public class VideoDetailAdapter extends ArticleDetailAdapter {
    public VideoDetailAdapter(Context context, ArrayList<Map<String, String>> list, String type, String code) {
        super(context, list, type, code);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return super.getView(position, convertView, parent);
    }
}

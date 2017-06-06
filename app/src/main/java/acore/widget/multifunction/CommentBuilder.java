package acore.widget.multifunction;

import android.text.TextUtils;
import android.view.View;

import acore.widget.multifunction.base.StyleConfig;
import acore.widget.multifunction.base.StyleConfigBuilder;

/**
 * Created by Fang Ruijiao on 2017/5/25.
 */

public class CommentBuilder extends StyleConfigBuilder {

    private String text;
    private String textColor = "#535353";
    private String backgroundColor ;
    private int chooseColor;
    private int textSize;

    public CommentBuilder(String text) {
        super();
        this.text = text;
    }

    @Override
    public CommentBuilder setTextColor(String color){
        textColor = color;
        return this;
    }

    @Override
    public CommentBuilder setBackgroudColor(String color){
        backgroundColor = color;
        return this;
    }

    public void setChoseBackColor(int color){
        chooseColor = color;
    }

    public CommentBuilder setTextSize(int px){
        textSize = px;
        return this;
    }

    public void parse(final CommentClickCallback callback) {
        StyleConfig config = new StyleConfig(text);
        config.setTextColor(textColor);
        if(textSize > 0)
            config.setTextSize(textSize);
        if(!TextUtils.isEmpty(backgroundColor))
            config.setBackgroudColor(backgroundColor);
        config.setStart(0);
        config.setEnd(text.length());
        config.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(callback != null){
                    callback.onCommentClick(v,"");
                }
            }
        });
        configs.add(config);
    }

    public String getContent() {
        return text;
    }



    public interface CommentClickCallback {
        public void onCommentClick(View v, String userCode);
    }
}

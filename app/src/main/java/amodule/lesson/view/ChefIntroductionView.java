package amodule.lesson.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.xiangha.R;

import java.util.ArrayList;
import java.util.List;

import acore.widget.OverlayViewPager;

/**
 * Description :
 * PackageName : amodule.lesson.view
 * Created by mrtrying on 2018/12/4 19:03.
 * e_mail : ztanzeyu@gmail.com
 */
public class ChefIntroductionView extends FrameLayout {
    final int LAYOUT_ID = R.layout.view_chef_introduction;
    private OverlayViewPager mOverlayViewPager;
    public ChefIntroductionView(@NonNull Context context) {
        super(context);
        initialize(context, null, 0);
    }

    public ChefIntroductionView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context, attrs, 0);
    }

    public ChefIntroductionView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context, attrs, defStyleAttr);
    }

    private void initialize(Context context, AttributeSet attrs, int defStyleAttr) {
        LayoutInflater.from(context).inflate(LAYOUT_ID, this);
        mOverlayViewPager = findViewById(R.id.overlay_view);
        //TODO
        List<View> views = new ArrayList<>();
        for(int i=0;i<5;i++){
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_chef_introduction,null);
            views.add(view);
        }
        OverlayAdapter adapter = new OverlayAdapter();
        adapter.setData(views);
        mOverlayViewPager.init(adapter);

    }

    class OverlayAdapter extends OverlayViewPager.Adapter<View>{

        @Override
        public Object overWriteInstantiateItem(ViewGroup container, int position) {
            View view = LayoutInflater.from(getContext()).inflate(R.layout.item_chef_introduction,null);
            container.addView(view);
            return view;
        }
    }
}

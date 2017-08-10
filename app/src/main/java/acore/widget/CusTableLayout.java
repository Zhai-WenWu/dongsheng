package acore.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * PackageName : com.yyhd.ask.widget
 * Created by MrTrying on 2016/6/17 17:50.
 * E_mail : ztanzeyu@gmail.com
 */
public class CusTableLayout extends LinearLayout {
    private CustomTableAdapter adapter;
    private Context mContext;
    private Map<Integer, OnItemClickListenerById> mListenerMap;
    private OnItemSelectedListener itemSelectedListener;
    private List<View> items;
    private int row = 1;

    private float widthAndHieghtScale = 0;

    private int dashWidth = 0;
    private int layoutRes = 0;

    public CusTableLayout(Context context) {
        this(context, null);
    }

    public CusTableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        items = new ArrayList<>();
        mListenerMap = new HashMap<>();
        setOrientation(LinearLayout.VERTICAL);
    }

    public void setCustomTable(int row, List<Map<String, String>> data, int resource, String[] from, int[] to, SimpleAdapter.ViewBinder viewBinder) {
        this.row = row;
        this.layoutRes = resource;
        adapter = new CustomTableAdapter(mContext, data, resource, from, to);
        if (viewBinder != null) {
            adapter.setViewBinder(viewBinder);
        }
        addChildView(data);
    }

    //刷新FlowLayout布局
    public void refreshLayout() {
        adapter.notifyDataSetChanged();
        this.removeAllViews();
        addChildView(adapter.data);
    }

    //设置数据
    public void setData(List<Map<String, String>> data) {
        adapter.data = data;
        refreshLayout();
    }

    /**
     * 根据data添加childView
     *
     * @param data
     */
    private void addChildView(List<Map<String, String>> data) {
        //addView前先移除所有view
        if (getChildCount() > 0) {
            removeAllViews();
        }
        //添加view
        int tatolLine = data.size() / row;
        if (data.size() % row > 0) {
            tatolLine++;
        }
        int width = LayoutParams.MATCH_PARENT;
        int hieght = LayoutParams.WRAP_CONTENT;
        if (widthAndHieghtScale != 0) {
            width = getWindowPx(mContext).widthPixels / row;
            hieght = (int) (width / widthAndHieghtScale);
        }
        //创建所有item
        for (int i = 0; i < data.size(); i++) {
            View view = adapter.getView(i, null, this);
            int currentX = i % row;
            int currentY = i / row;
            LinearLayout rowLayout = null;
            if (currentX == 0) {
                rowLayout = new LinearLayout(mContext);
                rowLayout.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
                if (currentY != tatolLine - 1) {
                    layoutParams.setMargins(0, 0, 0, dashWidth);
                }
                this.addView(rowLayout, layoutParams);
            } else {
                rowLayout = (LinearLayout) getChildAt(currentY);
            }
            rowLayout.setWeightSum(row);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, hieght);
            if (currentX != row - 1) {
                layoutParams.setMargins(0, 0, dashWidth, 0);
            }
            layoutParams.weight = 1f;
            rowLayout.addView(view, layoutParams);
            //绑定点击事件
            Iterator<Map.Entry<Integer, OnItemClickListenerById>> it = mListenerMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, OnItemClickListenerById> entry = it.next();
                setClickListener(view, i, entry.getKey(), entry.getValue());
            }
            items.add(view);
        }
        //填充不足的item
        LinearLayout rowLayout = (LinearLayout) getChildAt(tatolLine - 1);
        final int length = tatolLine * row - data.size();
        for (int index = 0; index < length; index++) {
            View view = LayoutInflater.from(mContext).inflate(layoutRes, null);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, hieght);
            layoutParams.weight = 1f;
            if (index != length - 1) {
                layoutParams.setMargins(0, 0, dashWidth, 0);
            }
            rowLayout.addView(view, layoutParams);
        }
    }

    /**
     * 添加childView的click
     *
     * @param id       设置click的id(如果id为0则设置childView的click)
     * @param listener 需要这是的listener
     */
    public void setOnItemClickListenerById(Integer id, final OnItemClickListenerById listener) {
        mListenerMap.put(id, listener);
    }

    /**
     * @param rootView
     * @param position
     * @param id
     * @param listener
     */
    private void setClickListener(View rootView, final int position, Integer id, final OnItemClickListenerById listener) {
        View clickView;
        if (id != 0) {
            clickView = rootView.findViewById(id);
        } else {
            clickView = rootView;
        }
        clickView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v, position);
                updateSelection(position);
            }
        });
    }

    public void setSelection(int position) {
        updateSelection(position);
    }

    public void setSelection(int row, int column) {
        updateSelection(column * this.row + row);
    }

    private void updateSelection(int position) {
        for (int i = 0; i < items.size(); ++i) {
            View view = items.get(i);
            final boolean selected = i == position;
            view.setSelected(selected);
            if (selected) {
                select(view, i);
                if (itemSelectedListener != null) {
                    itemSelectedListener.onSelected(view, position);
                }
            } else {
                unselect(view, i);
                if (itemSelectedListener != null) {
                    itemSelectedListener.onUnselected(view, position);
                }
            }
        }
    }

    private void unselect(View view, int position) {

    }

    private void select(View view, int position) {
    }

    /** 获取手机像素高宽 */
    public DisplayMetrics getWindowPx(Context context) {
        DisplayMetrics metric = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (manager != null) {
            manager.getDefaultDisplay().getMetrics(metric);
        }
        return metric;
    }


    public class CustomTableAdapter extends SimpleAdapter {
        private List<Map<String, String>> data;

        @SuppressWarnings("unchecked")
        public CustomTableAdapter(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            setData((List<Map<String, String>>) data);
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);

            return view;
        }

        public List<Map<String, String>> getData() {
            return data;
        }

        public void setData(List<Map<String, String>> data) {
            this.data = data;
        }
    }

    public interface OnItemClickListenerById {
        void onClick(View v, int position);
    }

    public interface OnItemSelectedListener {
        void onSelected(View v, int position);

        void onUnselected(View v, int position);

    }

    public float getWidthAndHieghtScale() {
        return widthAndHieghtScale;
    }

    public void setWidthAndHieghtScale(float widthAndHieghtScale) {
        this.widthAndHieghtScale = widthAndHieghtScale;
    }

    public int getDashWidth() {
        return dashWidth;
    }

    public void setDashWidth(int dashWidth) {
        this.dashWidth = dashWidth;
    }

}


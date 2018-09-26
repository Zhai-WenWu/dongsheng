package acore.logic.stat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiangha.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import acore.logic.SpecialOrder;
import acore.tools.Tools;
import acore.tools.ToolsDevice;
import acore.widget.rvlistview.RvListView;
import acore.widget.rvlistview.adapter.RvBaseAdapter;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

import static acore.logic.stat.StatisticsManager.EVENT_BTN_CLICK;
import static acore.logic.stat.StatisticsManager.EVENT_LIST_CLICK;
import static acore.logic.stat.StatisticsManager.EVENT_LIST_SHOW;
import static acore.logic.stat.StatisticsManager.EVENT_SPECIAL_ACTION;
import static acore.logic.stat.StatisticsManager.EVENT_STAY;
import static acore.logic.stat.StatisticsManager.EVENT_VIDEO_VIEW;

/**
 * Description :
 * PackageName : acore.logic.statistics
 * Created by mrtrying on 2018/8/6 18:22.
 * e_mail : ztanzeyu@gmail.com
 */
public class DesktopLayout extends RelativeLayout {
    @SuppressLint("StaticFieldLeak") private volatile static DesktopLayout instance;
    private RvListView rvListView;
    private StatisticsAdapter adapter;
    private List<String> data = new ArrayList<>();

    public static DesktopLayout of(Context context) {
        if (instance == null) {
            synchronized (DesktopLayout.class) {
                if (instance == null) {
                    instance = new DesktopLayout(context);
                }
            }
        }
        return instance;
    }

    private DesktopLayout(Context context) {
        super(context);
        initUI();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initUI() {
        View root = LayoutInflater.from(getContext()).inflate(R.layout.view_statistics_layout, null);
        addView(root, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        ImageView imageView = findViewById(R.id.image);
        rvListView = findViewById(R.id.rvListView);
        adapter = new StatisticsAdapter(getContext(), data);
        rvListView.setAdapter(adapter);
        findViewById(R.id.clean).setOnClickListener(v -> {
            data.clear();
            adapter.notifyDataSetChanged();
        });
        imageView.setOnTouchListener(new View.OnTouchListener() {
            float mTouchStartX;
            float mTouchStartY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // 获取相对屏幕的坐标，即以屏幕左上角为原点
                x = event.getRawX();
                y = event.getRawY() - top; // 25是系统状态栏的高度
                Log.i("startP", "startX" + mTouchStartX + "====startY" + mTouchStartY);
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // 获取相对View的坐标，即以此View左上角为原点
                        mTouchStartX = event.getX();
                        mTouchStartY = event.getY();
                        Log.i("startP", "startX" + mTouchStartX + "====startY" + mTouchStartY);
                        long end = System.currentTimeMillis() - startTime;
                        // 双击的间隔在 300ms以下
                        if (end < 500) {
                            SpecialOrder.switchStatLayoutVisibility(getContext());
                        }
                        startTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        // 更新浮动窗口位置参数
                        mLayout.x = (int) (x - mTouchStartX);
                        mLayout.y = (int) (y - mTouchStartY);
                        mWindowManager.updateViewLayout(DesktopLayout.this, mLayout);
                        break;
                    case MotionEvent.ACTION_UP:
                        // 更新浮动窗口位置参数
                        mLayout.x = (int) (x - mTouchStartX);
                        mLayout.y = (int) (y - mTouchStartY);
                        mWindowManager.updateViewLayout(DesktopLayout.this, mLayout);
                        // 可以在此记录最后一次的位置
                        mTouchStartX = mTouchStartY = 0;
                        break;
                }
                return true;
            }
        });
        createWindowManager();

    }

    public void insertData(String text) {
        this.data.add(0, text);
        adapter.notifyDataSetChanged();
        rvListView.scrollToPosition(0);
        showDesk();
    }

    class StatisticsAdapter extends RvBaseAdapter<String> {

        public StatisticsAdapter(Context context, @Nullable List<String> data) {
            super(context, data);
        }

        @Override
        public RvBaseViewHolder<String> onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_statistics, parent, false);
            return new StatisticsHolder(itemView);
        }

        @Override
        public int getItemViewType(int position) {
            return 0;
        }
    }

    class StatisticsHolder extends RvBaseViewHolder<String> {
        TextView textView;

        public StatisticsHolder(@NonNull View itemView) {
            super(itemView);
            textView = findViewById(R.id.text);
        }

        @Override
        public void bindData(int position, @Nullable String data) {
            try {
                if (data.contains("\"" + EVENT_STAY + "\"")) {
                    textView.setTextColor(Color.parseColor("#0AFF1F"));
                } else if (data.contains("\"" + EVENT_LIST_SHOW + "\"")) {
                    textView.setTextColor(Color.parseColor("#FF8200"));
                } else if (data.contains("\"" + EVENT_BTN_CLICK + "\"")) {
                    textView.setTextColor(Color.parseColor("#FFFA1C"));
                } else if (data.contains("\"" + EVENT_LIST_CLICK + "\"")) {
                    textView.setTextColor(Color.parseColor("#ffffff"));
                } else if (data.contains("\"" + EVENT_VIDEO_VIEW + "\"")) {
                    textView.setTextColor(Color.parseColor("#FF004D"));
                } else if (data.contains("\"" + EVENT_SPECIAL_ACTION + "\"")) {
                    textView.setTextColor(Color.parseColor("#ffffff"));
                } else {
                    textView.setTextColor(Color.parseColor("#ffffff"));
                }
                JSONObject jsonObject = new JSONObject(data);
                textView.setText(Uri.decode(jsonObject.toString(4)));
            } catch (JSONException e) {
                textView.setText(Uri.decode(data));
                e.printStackTrace();
            }
        }
    }

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayout;
    private long startTime;
    // 声明屏幕的宽高
    float x, y;
    int top;
    boolean isShow = false;

    /** 显示DesktopLayout */
    public void showDesk() {
        if (!isShow && instance != null) {
            isShow = true;
            mWindowManager.addView(instance, mLayout);
            instance.requestLayout();
        }
    }

    /** 关闭DesktopLayout */
    public void closeDesk() {
        if (isShow && instance != null) {
            isShow = false;
            mWindowManager.removeView(instance);
        }
    }

    /**
     * 设置WindowManager
     */
    @SuppressLint("WrongConstant")
    private void createWindowManager() {
        // 取得系统窗体
        mWindowManager = (WindowManager) getContext().getApplicationContext().getSystemService("window");

        // 窗体的布局样式
        mLayout = new WindowManager.LayoutParams();

        // 设置窗体显示类型——TYPE_SYSTEM_ALERT(系统提示)
        mLayout.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        // 设置窗体焦点及触摸：
        // FLAG_NOT_FOCUSABLE(不能获得按键输入焦点)
        mLayout.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

        // 设置显示的模式
        mLayout.format = PixelFormat.RGBA_8888;

        // 设置对齐的方法
        mLayout.gravity = Gravity.TOP | Gravity.LEFT;

        // 设置窗体宽度和高度
        mLayout.width = Tools.getDimen(getContext(), R.dimen.dp_240);
        mLayout.height = (int) (ToolsDevice.getWindowPx(getContext()).heightPixels * 3 / 4f);

    }

}

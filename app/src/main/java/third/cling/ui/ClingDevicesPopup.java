package third.cling.ui;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.xiangha.R;

import java.util.Collection;
import java.util.Iterator;

import third.cling.control.OnDeviceSelectedListener;
import third.cling.entity.ClingDevice;
import third.cling.entity.DevicesAdapter;
import third.cling.entity.IDevice;
import third.cling.service.manager.ClingManager;
import third.cling.util.Utils;

/**
 * Created by sll on 2017/11/28.
 */

public class ClingDevicesPopup extends PopupWindow {

    private View mContentView;
    private TextView mHeaderTitle;
    private View mEmptyView;
    private ImageView mCloseImage;
    private ListView mListView;
    private DevicesAdapter mDevicesAdapter;
    private ClingDevice mSelectedDevice;
    private View mSelectedItem;

    private OnDeviceSelectedListener mOnDeviceSelected;
    private DataSetObserver mDataSetObserver;
    public ClingDevicesPopup(Context context) {
        super(context);
        initView(context);
        initData(context);
        addListener();
    }

    private void initView(Context context) {
        mContentView = LayoutInflater.from(context).inflate(R.layout.cling_devices_layout, null);
        setContentView(mContentView);
        setBackgroundDrawable(new ColorDrawable(Color.parseColor("#70000000")));
        setWidth(WindowManager.LayoutParams.MATCH_PARENT);
        setHeight(WindowManager.LayoutParams.MATCH_PARENT);
        setFocusable(true);
        setOutsideTouchable(true);
        setAnimationStyle(R.style.BottomInOutPopupAnim);
        mEmptyView = mContentView.findViewById(R.id.empty_view);
        mHeaderTitle = (TextView) mContentView.findViewById(R.id.title);
        mHeaderTitle.setText("选择要投屏的设备");
        mHeaderTitle.setTextColor(Color.parseColor("#666666"));
        mCloseImage = (ImageView) mContentView.findViewById(R.id.icon);
        mCloseImage.setImageResource(R.drawable.search_his_close);
        mCloseImage.setVisibility(View.VISIBLE);
        mListView = (ListView) mContentView.findViewById(R.id.devices_list);
    }

    private void initData(Context context) {
        mDevicesAdapter = new DevicesAdapter(context);
        mListView.setAdapter(mDevicesAdapter);
    }

    private void addListener() {
        View.OnClickListener listener = v -> ClingDevicesPopup.this.dismiss();
        mContentView.setOnClickListener(listener);
        mCloseImage.setOnClickListener(listener);
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            ClingDevicesPopup.this.dismiss();
            // 选择连接设备
            ClingDevice item = mDevicesAdapter.getItem(position);
            if (Utils.isNull(item)) {
                return;
            }
            boolean canSelected = false;
            if (mSelectedDevice == null) {
                mSelectedDevice = item;
                mSelectedItem = view;
                mDevicesAdapter.setDeviceSelected(view, true);
                canSelected = true;
            } else if (!item.getDevice().equals(mSelectedDevice.getDevice())){
                mDevicesAdapter.setDeviceSelected(mSelectedItem, false);
                mSelectedDevice = item;
                mSelectedItem = view;
                mDevicesAdapter.setDeviceSelected(mSelectedItem, true);
                canSelected = true;
            }
            if (canSelected) {
                ClingManager.getInstance().setSelectedDevice(item);
                mOnDeviceSelected.onDeviceSelected(mSelectedDevice);
            }
        });
        mDataSetObserver = new DataSetObserver() {
            @Override
            public void onChanged() {
                mEmptyView.post(() -> {
                    if (ClingDevicesPopup.this.isShowing())
                        mEmptyView.setVisibility(mDevicesAdapter.isEmpty() ? View.VISIBLE : View.GONE);
                });
            }

            @Override
            public void onInvalidated() {
                super.onInvalidated();
            }
        };
        mDevicesAdapter.registerDataSetObserver(mDataSetObserver);
    }

    public void addDevice(IDevice device) {
        mDevicesAdapter.add((ClingDevice) device);
    }

    public void removeDevice(IDevice device) {
        mDevicesAdapter.remove((ClingDevice) device);
    }

    public void clear() {
        mDevicesAdapter.clear();
    }

    public void addAll(Collection<? extends ClingDevice> devices) {
        if (mSelectedDevice == null)
            mDevicesAdapter.addAll(devices);
        else {
            Iterator<ClingDevice> iterator = (Iterator<ClingDevice>) devices.iterator();
            while (iterator.hasNext()) {
                ClingDevice device = iterator.next();
                if (mSelectedDevice.getDevice().equals(device.getDevice())) {
                    device.setSelected(true);
                    break;
                }
            }
            mDevicesAdapter.addAll(devices);
        }
    }

    public void setOnDeviceSelected(OnDeviceSelectedListener listener) {
        mOnDeviceSelected = listener;
    }

    public void destroySelectedDevice() {
        mSelectedDevice = null;
    }

    public void destroyPopup() {
        if (mDevicesAdapter != null && mDataSetObserver != null)
            mDevicesAdapter.unregisterDataSetObserver(mDataSetObserver);
        mOnDeviceSelected = null;
        mDataSetObserver = null;
    }
}

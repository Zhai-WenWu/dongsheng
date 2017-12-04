package third.cling.entity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

/**
 * 显示设备列表的适配器
 */

public class DevicesAdapter extends ArrayAdapter<ClingDevice> {
    private LayoutInflater mInflater;

    public DevicesAdapter(Context context) {
        super(context, 0);
        mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHoler = null;
        if (convertView == null || (convertView.getTag() != null && !(convertView.getTag() instanceof ViewHolder))) {
            convertView = mInflater.inflate(R.layout.cling_devices_itemlayout, null);
            viewHoler = new ViewHolder(convertView);
            convertView.setTag(viewHoler);
        } else
            viewHoler = (ViewHolder) convertView.getTag();
        ClingDevice item = getItem(position);
        if (item == null || item.getDevice() == null) {
            return convertView;
        }
        viewHoler.setData(item);
        return convertView;
    }

    public void setDeviceSelected(View item, boolean selected) {
        item.findViewById(R.id.icon).setVisibility(selected ? View.VISIBLE : View.GONE);
    }

    private class ViewHolder {
        private View mItemView;
        private TextView mDeviceName;
        private ImageView mSelectView;

        public ViewHolder(View itemView) {
            mItemView = itemView;
            if (mItemView == null)
                return;
            mDeviceName = (TextView) mItemView.findViewById(R.id.title);
            mSelectView = (ImageView) mItemView.findViewById(R.id.icon);
        }

        public void setData(ClingDevice device) {
            mDeviceName.setText(device.getDevice().getDetails().getFriendlyName());
            mSelectView.setVisibility(device.isSelected() ? View.VISIBLE : View.GONE);
        }
    }
}
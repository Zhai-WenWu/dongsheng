package third.cling.entity;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xiangha.R;

import org.fourthline.cling.model.meta.Device;

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
            convertView = mInflater.inflate(R.layout.devices_items, null);
            viewHoler = new ViewHolder(convertView);
            convertView.setTag(viewHoler);
        } else
            viewHoler = (ViewHolder) convertView.getTag();
        ClingDevice item = getItem(position);
        if (item == null || item.getDevice() == null) {
            return convertView;
        }
        viewHoler.setData(item.getDevice());
        return convertView;
    }

    private class ViewHolder {
        private View mItemView;
        private TextView mDeviceName;
        private ImageView mDeviceImg;

        public ViewHolder(View itemView) {
            mItemView = itemView;
            if (mItemView == null)
                return;
            mDeviceImg = (ImageView) mItemView.findViewById(R.id.listview_item_image);
            mDeviceName = (TextView) mItemView.findViewById(R.id.listview_item_line_one);
            setDefaultData();
        }

        private void setDefaultData() {
            mDeviceImg.setImageResource(R.drawable.ic_action_dock);
        }

        public void setData(Device device) {
            mDeviceName.setText(device.getDetails().getFriendlyName());
        }
    }
}
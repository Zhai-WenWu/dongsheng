/*
 * Copyright (C) 2017 mrtrying
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package acore.widget.rvlistview;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

/**
 * Description : //TODO
 * PackageName : acore.widget.rvlistview
 * Created by MrTrying on 2017/9/28 16:32.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public class RvBaseSimpleAdapter extends RvBaseAdapter {

    private static final String TAG = Config.TAG + " :: " + RvBaseSimpleAdapter.class.getSimpleName();

    private final LayoutInflater mInflater;

    private int mResource;

    private String[] mFrom;
    private int[] mTo;

    private ViewBinder mViewBinder;

    public RvBaseSimpleAdapter(Context context, @Nullable List<Map<String, ? extends Object>> data,
                               @LayoutRes int resource, String[] from, @IdRes int[] to) {
        super(context, data);
        this.mInflater = LayoutInflater.from(context);
        this.mResource = resource;
        this.mFrom = from;
        this.mTo = to;
    }

    @Override
    public RvBaseViewHolder<Map<String, String>> onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = createViewFromResource(parent);
        return createViewHolder(itemView);
    }

    private View createViewFromResource(ViewGroup parent) {
        return mInflater.inflate(mResource, parent,false);
    }

    private RvBaseViewHolder<Map<String, String>> createViewHolder(View itemView) {
        return new RvBaseViewHolder<Map<String, String>>(itemView) {
            @Override
            public void bindData(int position, Map<String, String> data) {
                RvBaseSimpleAdapter.this.bindData(this, position, data);
            }
        };
    }

    @Override
    public void onBindViewHolder(RvBaseViewHolder holder, int position) {
        holder.bindData(position, getItem(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return null == mData ? 0 : mData.size();
    }

    private void bindData(RvBaseViewHolder holder, int position, final Map<String, ?> dataSet) {
        if (null == dataSet)
            return;

        final ViewBinder binder = mViewBinder;
        final String[] from = mFrom;
        final int[] to = mTo;
        final int count = to.length;

        for (int i = 0; i < count; i++) {
            final View v = holder.findViewById(to[i]);
            if (v != null) {
                final Object data = dataSet.get(from[i]);
                String text = data == null ? "" : data.toString();
                if (text == null) {
                    text = "";
                }

                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, data, text);
                }

                if (!bound) {
                    if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked((Boolean) data);
                        } else if (v instanceof TextView) {
                            // Note: keep the instanceof TextView check at the bottom of these
                            // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                            setViewText((TextView) v, text);
                        } else {
                            throw new IllegalStateException(v.getClass().getName() +
                                    " should be bound to a Boolean, not a " +
                                    (data == null ? "<unknown type>" : data.getClass()));
                        }
                    } else if (v instanceof TextView) {
                        // Note: keep the instanceof TextView check at the bottom of these
                        // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                        setViewText((TextView) v, text);
                    } else if (v instanceof ImageView) {
                        if (data instanceof Integer) {
                            setViewImage((ImageView) v, (Integer) data);
                        } else {
                            setViewImage((ImageView) v, text);
                        }
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }
    }

    public void setViewImage(ImageView v, int value) {
        v.setImageResource(value);
    }

    public void setViewImage(ImageView v, String value) {
        try {
            v.setImageResource(Integer.parseInt(value));
        } catch (NumberFormatException nfe) {
            v.setImageURI(Uri.parse(value));
        }
    }

    public void setViewText(TextView v, String text) {
        v.setText(text);
    }
    
    /*------------------------------------------------------- Inner Class -------------------------------------------------------*/

    public static interface ViewBinder {
        boolean setViewValue(View view, Object data, String textRepresentation);
    }
    
    /*------------------------------------------------------- Get&Set -------------------------------------------------------*/

    public ViewBinder getViewBinder() {
        return mViewBinder;
    }

    public void setViewBinder(ViewBinder mViewBinder) {
        this.mViewBinder = mViewBinder;
    }
}

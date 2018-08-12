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

package acore.widget.rvlistview.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;


import java.util.ArrayList;
import java.util.List;

import acore.widget.rvlistview.Config;
import acore.widget.rvlistview.holder.RvBaseViewHolder;

/**
 * 基础adapter
 */
public abstract class RvBaseAdapter<T> extends BaseAdapter<T, RvBaseViewHolder<T>> {

    public final static String TAG = Config.TAG + " :: " + RvBaseAdapter.class.getSimpleName();

    public RvBaseAdapter(Context context, @Nullable List<T> data) {
        super(context, data);
    }

    public abstract int getItemViewType(int position);
}

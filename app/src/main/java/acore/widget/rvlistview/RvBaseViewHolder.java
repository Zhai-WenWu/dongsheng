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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;

/**
 * Description : //TODO
 * PackageName : acore.widget.rvlistview
 * Created by MrTrying on 2017/9/28 15:31.
 * Author : mrtrying
 * E_mail : ztanzeyu@gmail.com
 */

public abstract class RvBaseViewHolder<T> extends RecyclerView.ViewHolder {

    private static final String TAG = Config.TAG + " :: " + RvBaseViewHolder.class.getSimpleName();

    private SparseArray<View> mViews;

    public RvBaseViewHolder(@NonNull View itemView) {
        super(itemView);
        mViews = new SparseArray<>();
        //初始化view
    }

    public abstract void bindData(int position, @Nullable T data);

    /**快速获取view*/
    public <V extends View> V findViewById(int viewId){
        View view = mViews.get(viewId);
        if(null == view){
            view = itemView.findViewById(viewId);
            if(null != view){
                mViews.put(viewId,view);
            }else{
                return null;
            }
        }
        return (V) view;
    }
}

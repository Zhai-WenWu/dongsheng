package aplug.recordervideo.tools;

import android.hardware.Camera;

import java.util.Comparator;

/**
 * Created by XiangHa on 2016/10/13.
 */

public class SortComparator implements Comparator {
    /**
     * 排序方法
     * @param lhs : 第一个对象
     * @param rhs : 第二个对象
     * @return > 0 表示第一个对象比第二个对象大
     */
    @Override
    public int compare(Object lhs, Object rhs) {
        Camera.Size size1 = (Camera.Size)lhs;
        Camera.Size size2 = (Camera.Size)rhs;
        if(size1.width == 1920){
            return -1;
        }else if(size2.width == 1920){
            return 1;
        }
        if(size1.width > size2.width)
            return -1;
        else
            return 1;
    }
}

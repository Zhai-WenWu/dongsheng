package third.cling.util;

import java.util.Collection;

/**
 * 说明：
 *
 * 日期：17/6/28 16:54
 */

public class ListUtils {

    public static boolean isEmpty(Collection list){
        return !(list != null && list.size() != 0);
    }

}

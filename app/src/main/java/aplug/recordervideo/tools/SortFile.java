package aplug.recordervideo.tools;

import java.io.File;
import java.util.Comparator;

/**
 * Created by Fang Ruijiao on 2016/10/21.
 */

public class SortFile implements Comparator<File> {
    @Override
    public int compare(File lhs, File rhs) {
        if(lhs.lastModified() < rhs.lastModified()){
            return -1;//最后修改的视频在前
        }else{
            return 1;
        }
    }
}

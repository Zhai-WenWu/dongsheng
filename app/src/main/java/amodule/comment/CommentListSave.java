package amodule.comment;

import java.util.ArrayList;
import java.util.Map;

public class CommentListSave {

    public static ArrayList<Map<String, String>> mList;

    public static void saveList(ArrayList<Map<String, String>> listArray) {
        mList = listArray;
    }
}

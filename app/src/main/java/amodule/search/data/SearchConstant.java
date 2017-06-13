package amodule.search.data;

/**
 * Created by ：airfly on 2016/10/11 09:45.
 */

public interface SearchConstant {

    //搜索类型 菜谱 哈友 贴子
    int SEARCH_CAIPU = 0;
    int SEARCH_HAYOU = 1;
    int SEARCH_MEISHITIE = 2;


    String SEARCH_WORD = "search_word";
    String SEARCH_TYPE = "search_type";

    int VIEW_DEFAULT_SEARCH = 1;
    int VIEW_MATCH_WORDS = 2;
    int VIEW_CAIPU_RESULT = 3;
    int VIEW_HAYOU_RESULT = 4;
    int VIEW_TIEZI_RESULT = 5;

    String[] SEARCH_CATEGORY = {"搜菜谱", "搜哈友", "搜贴子"};

}

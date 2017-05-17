package aplug.imageselector.constant;

/**
 * @author Eva
 *
 */
public class ImageSelectorConstant {
	
	/** 最大图片选择次数，int类型，默认8 */
    public static final String EXTRA_SELECT_COUNT = "max_select_count";
    /** 图片选择模式，默认多选 */
    public static final String EXTRA_SELECT_MODE = "select_count_mode";
    /** 是否显示相机，默认显示 */
    public static final String EXTRA_SHOW_CAMERA = "show_camera";
    /** 选择结果，返回为 ArrayList<String> 图片路径集合  */
    public static final String EXTRA_RESULT = "select_result";
    /** 默认选择集 */
    public static final String EXTRA_DEFAULT_SELECTED_LIST = "default_list";
    /** 不可选图片集合 */
    public static final String EXTRA_NOT_SELECTED_LIST = "not_selected_list";
    /** 来自哪个页面 */
    public static final String OPEN_FROM = "open_from";
    /** 单选 */
    public static final int MODE_SINGLE = 0;
    /** 多选 */
    public static final int MODE_MULTI = 1;
    /** 默认图片数量 */
    public static final int DEFAULTCOUNT = 8;
    /** 是否是提交操作 */
    public static final String EXTRA_IS_COMMIT = "is_commit";

}

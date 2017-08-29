package third.mall.aplug;

import xh.basic.tool.UtilString;

public class MallStringManager extends UtilString {

    public final static String defaultDomain = ".ds.xiangha.com";
//    	public final static String defaultDomain = ".ds.mamaweiyang.net:7888";
    public static String apiAPI = "api";
    public static String appm = "m";
    public static String httpData = "http://";

    public static String apiTitle = httpData + apiAPI;
    public static String appWebTitle = httpData + appm;
    //当前域名
    public static String domain = defaultDomain;
    //API请求地址
    public static String mall_apiUrl = apiTitle + defaultDomain;
    public static String mall_web_apiUrl = appWebTitle + defaultDomain;

    //	public static String mall_apiUrl = "http://api.ds.mamaweiyang.net:7888";
//	public static String mall_web_apiUrl = "http://m.ds.mamaweiyang.net:7888";
    public final static String mall_api_register = mall_apiUrl + "/v1/customer/register";
    public final static String mall_api_home = mall_apiUrl + "/v1/home";
    public final static String mall_api_product_info = mall_apiUrl + "/v1/product/info";
    public final static String mall_api_product_info_v3 = mall_apiUrl + "/v3/product/info";
    public final static String mall_api_computeOrderAmt = mall_apiUrl + "/v1/order/computeOrderAmt";
    public final static String mall_api_createOrder = mall_apiUrl + "/v1/order/createOrder";
    public final static String mall_api_getShippingAddress = mall_apiUrl + "/v1/customer/getShippingAddress";
    public final static String mall_api_listOrder = mall_apiUrl + "/v1/order/listOrder";

    public final static String mall_api_getprovinces = mall_apiUrl + "/v1/address/getprovinces";
    public final static String mall_api_getcitys = mall_apiUrl + "/v1/address/getcitys";
    public final static String mall_api_getcountys = mall_apiUrl + "/v1/address/getcountys";
    public final static String mall_api_gettowns = mall_apiUrl + "/v1/address/gettowns";

    public final static String mall_api_direct = mall_apiUrl + "/v1/payment/direct";
    public final static String mall_api_orderComplete = mall_apiUrl + "/v1/order/orderComplete";

    public final static String mall_api_addShippingAddress = mall_apiUrl + "/v1/customer/addShippingAddress";
    public final static String mall_api_setShippingAddress = mall_apiUrl + "/v1/customer/setShippingAddress";
    public final static String mall_api_getShippingAddressDetail = mall_apiUrl + "/v1/customer/getShippingAddressDetail";
    public final static String mall_api_order_info = mall_apiUrl + "/v1/order/info";
    public final static String mall_api_order_info_v2 = mall_apiUrl + "/v2/order/info";
    public final static String mall_api_delOrder = mall_apiUrl + "/v1/order/delOrder";
    //web页面
    public final static String api_mallHome = mall_web_apiUrl + "/v1/home?fr1=ds_home_entry1";//电商首页
    public final static String mall_web_shangjia = mall_web_apiUrl + "/v1/login";
    public final static String mall_web_shop_detail = mall_web_apiUrl + "/v1/shop/detail";
    public final static String mall_web_product_detail = mall_web_apiUrl + "/v1/product/detail";
    public final static String mall_web_topic_getTopicInfo = mall_web_apiUrl + "/v1/topic/getTopicInfo";
    public final static String mall_web_shop_home = mall_web_apiUrl + "/v1/shop/home";
    public final static String mall_web_search_index = mall_web_apiUrl + "/v1/search/index";
    public final static String mall_web_couponSet = mall_web_apiUrl + "/v1/coupon/couponSet";
    public final static String mall_web_classify = mall_web_apiUrl + "/v1/classify/classifyInfo";

    //获取dsToken
    public final static String mall_getDsToken = httpData + "oauth.xiangha.com/ds/getDsToken";
    //购物车
    public final static String mall_getCartProductNum = mall_apiUrl + "/v1/cart/getCartProductNum";
    public final static String mall_getCartInfo = mall_apiUrl + "/v1/cart/getCartInfo";
    public final static String mall_updateCartInfo = mall_apiUrl + "/v1/cart/updateCartInfo";
    public final static String mall_delCartProudct = mall_apiUrl + "/v1/cart/delCartProudct";
    public final static String mall_addCartProduct = mall_apiUrl + "/v1/cart/addCartProduct";
    public final static String mall_checkoutOrder = mall_apiUrl + "/v1/order/checkoutOrder";
    public final static String mall_createOrderByCart = mall_apiUrl + "/v1/order/createOrderByCart";
    public final static String mall_createOrderByCart_v2 = mall_apiUrl + "/v2/order/createOrderByCart";
    public final static String mall_dsInfo = mall_apiUrl + "/v1/home/dsInfo";
    public final static String mall_getCartInfo_v2 = mall_apiUrl + "/v2/cart/getCartInfo";
    //物流信息
    public final static String mall_getShippingUrl = mall_apiUrl + "/v1/shipping/getShippingUrl";
    //第6版的接口
    public final static String mall_getShippingAddress = mall_apiUrl + "/v1/customer/getShippingAddress";
    public final static String mall_delShippingAddress = mall_apiUrl + "/v1/customer/delShippingAddress";
    public final static String mall_setDefaultAddress = mall_apiUrl + "/v1/customer/setDefaultAddress";
    public final static String mall_api_listOrder_v2 = mall_apiUrl + "/v2/order/listOrder";
    public final static String mall_cancelOrder = mall_apiUrl + "/v1/order/cancelOrder";
    public final static String mall_orderinfo = mall_apiUrl + "/v2/order/info";
    public final static String mall_addCartProductList = mall_apiUrl + "/v1/cart/addCartProductList";
    //优惠券
    public final static String mall_getShopCouponInfo = mall_apiUrl + "/v1/coupon/getShopCouponInfo";
    public final static String mall_getAShopCoupon = mall_apiUrl + "/v1/coupon/getAShopCoupon";
    public final static String mall_checkoutOrder_v2 = mall_apiUrl + "/v2/order/checkoutOrder";
    public final static String getShopCouponList = mall_apiUrl + "/v1/coupon/getShopCouponList";
    public final static String mall_hotWord = mall_apiUrl + "/v1/search/hotWord";
    public final static String mall_dish = mall_apiUrl + "/v1/advertise/dish";
    public final static String mall_getHotRecommend = mall_apiUrl + "/v1/product/getHotRecommend";
    //sign
    public final static String mall_getToken = mall_apiUrl + "/v2/sign/getToken";
    //详情页改版&评论
    public final static String mall_toComment = mall_apiUrl + "/v3/order/toComment";
    public final static String mall_addComment = mall_apiUrl + "/v3/comment/addComment";
    public final static String mall_addMuiltComment = mall_apiUrl + "/v3/comment/addMuiltComment";
    public final static String mall_api_listOrder_v3 = mall_apiUrl + "/v3/order/listOrder";
    public final static String mall_dsInfo_v3 = mall_apiUrl + "/v3/home/dsInfo";
    public final static String mall_api_getTemplate = mall_apiUrl + "/v3/template/getTemplate";

    /**
     * 替换url
     *
     * @param url
     * @return
     */
    public static String replaceUrl(String url) {
        if (domain != defaultDomain) {
            String[] find = new String[]{apiTitle, appWebTitle};
            String[] replace = new String[]{mall_apiUrl, mall_web_apiUrl};
            for (int i = 0; i < find.length; i++) {
                String findStr = find[i] + defaultDomain;
                if (url.indexOf(findStr) == 0) {
                    return url.replace(findStr, replace[i]);
                }
            }
        }
        return url;
    }

    //更换url
    public final static void changeUrl(String newDomain) {
        if (newDomain.length() > 1) {
            domain = newDomain;
            mall_apiUrl = apiTitle + domain;
            mall_web_apiUrl = appWebTitle + domain;
        }
    }
}

package amodule.main.Tools;

import android.text.TextUtils;

/**
 * Created by sll on 2017/6/23.
 */

public class ImageUtility {

    private static ImageUtility mUtility;

    private ImageUtility() {

    }

    public static synchronized ImageUtility getInstance() {
        if (mUtility == null)
            return new ImageUtility();
        return mUtility;
    }

    /**
     * Getting image size by url. eg: https://xxxxx/2122/212241289832.jpg/OTgwX2MxXzA?980_1307
     * @param url url must ends with "?100_100", it means that this image's width is 100px,height is 100px.
     * @param size an array of two integers in which to hold the width and the height.
     */
    public void getImageSizeByUrl(String url, int[] size) {
        if (!TextUtils.isEmpty(url)) {
            ImageSize is = new ImageSize();
            is.getImageSizeByUrl(url, size);
        }
    }

    private class ImageSize {
        private int mWidth;
        private int mHeight;

        private ImageSize() {

        }

        /**
         * Getting image size by url.
         * @param url url must ends with "?100_100", it means that this image's width is 100px,height is 100px.
         * @param size an array of two integers in which to hold the width and the height.
         */
        private void getImageSizeByUrl (String url, int[] size) {
            String[] sizes = url.split("\\?");
            if (sizes != null && sizes.length > 0) {
                String whStr = sizes[sizes.length - 1];
                if (!TextUtils.isEmpty(whStr)) {
                    String[] whs = whStr.split("_");
                    if (whs != null && whs.length == 2) {
                        String wStr = whs[0];
                        String hStr = whs[1];
                        if (!TextUtils.isEmpty(wStr) && !TextUtils.isEmpty(hStr)) {
                            try {
                                size[0] = Integer.parseInt(wStr);
                                size[1] = Integer.parseInt(hStr);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }
}

package amodule.upload.callback;

import amodule.upload.bean.UploadItemData;

/**
 * Created by ：fei_teng on 2016/10/30 16:55.
 */

  public  interface UploadListUICallBack {

    public void changeState();

    public void changeState(int pos, int index, UploadItemData data);

    public void uploadOver(boolean flag, String responseStr);


}

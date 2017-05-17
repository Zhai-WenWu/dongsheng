package third.mall.bean;

import java.io.Serializable;

/**
 *订单基础数据
 */

public class OrderBean implements Serializable{
    public String id = "";
    public String title = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

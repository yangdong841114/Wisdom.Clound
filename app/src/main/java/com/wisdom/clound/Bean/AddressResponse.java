package com.wisdom.clound.Bean;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * 地址列表接口返回数据封装
 */
public class AddressResponse {
    @SerializedName("code")
    private int code;

    @SerializedName("count")
    private int count;

    @SerializedName("msg")
    private String msg;

    @SerializedName("data")
    private List<UserAddress> data;

    // Getter & Setter
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<UserAddress> getData() {
        return data;
    }

    public void setData(List<UserAddress> data) {
        this.data = data;
    }
}

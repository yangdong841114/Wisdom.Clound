package com.wisdom.clound.Bean;

import org.json.JSONObject;

public class UserBean {
    private String nickname;    // 昵称
    private String avatarUrl;   // 头像URL
    private double redPacket;   // 红包
    private double coupon;      // 优惠券
    private double coin;        // 淘金币
    private int points;         // 积分

    // 空构造
    public UserBean() {}

    // 从JSON解析（适配你的接口返回格式）
    public static UserBean fromJson(String jsonStr) {
        UserBean userBean = new UserBean();
        try {
            JSONObject json = new JSONObject(jsonStr);
            userBean.setNickname(json.optString("nickname", ""));
            userBean.setAvatarUrl(json.optString("avatarUrl", ""));
            userBean.setRedPacket(json.optDouble("redPacket", 17.63));
            userBean.setCoupon(json.optDouble("coupon", 0));
            userBean.setCoin(json.optDouble("coin", 0.66));
            userBean.setPoints(json.optInt("points", 54));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userBean;
    }

    // Getter & Setter
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public double getRedPacket() { return redPacket; }
    public void setRedPacket(double redPacket) { this.redPacket = redPacket; }

    public double getCoupon() { return coupon; }
    public void setCoupon(double coupon) { this.coupon = coupon; }

    public double getCoin() { return coin; }
    public void setCoin(double coin) { this.coin = coin; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }
}

package com.monitor.main.model;

/**
 * Created by gaolixiao on 2017/4/22.
 */
public class LampInfo {

    private int lampCode;
    private String lampName;
    private int lampStatus;//灯的状态 0 代表关 1 代表 开

    public boolean isEditLampName() {
        return isEditLampName;
    }

    public void setEditLampName(boolean editLampName) {
        isEditLampName = editLampName;
    }

    private boolean isEditLampName = false;

    public int getLampCode() {
        return lampCode;
    }

    public void setLampCode(int lampCode) {
        this.lampCode = lampCode;
    }

    public int getLampStatus() {
        return lampStatus;
    }

    public void setLampStatus(int lampStatus) {
        this.lampStatus = lampStatus;
    }

    public String getLampName() {
        return lampName;
    }

    public void setLampName(String lampName) {
        this.lampName = lampName;
    }
}

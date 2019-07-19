package com.optical.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mariry on 2019/7/18.
 */
public class OpticalFibreTemp implements Serializable {

    private Long id;

    private Integer channel;

    //float数组的字符串
    private String tempData;

    //0:温度 1：Pa  2:Ps
    private Integer msgtype;

    private Date recdTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public String getTempData() {
        return tempData;
    }

    public void setTempData(String tempData) {
        this.tempData = tempData;
    }

    public Integer getMsgtype() {
        return msgtype;
    }

    public void setMsgtype(Integer msgtype) {
        this.msgtype = msgtype;
    }

    public Date getRecdTime() {
        return recdTime;
    }

    public void setRecdTime(Date recdTime) {
        this.recdTime = recdTime;
    }
}

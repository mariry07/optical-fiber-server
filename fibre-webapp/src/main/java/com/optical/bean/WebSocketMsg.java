package com.optical.bean;

import java.util.List;

/**
 * Created by mariry on 2019/11/14.
 */
public class WebSocketMsg {
    private Integer msgType;

    private Integer showWindow;

    private Object data;

    private Integer length;

    private List<Target2D> list;

    public Integer getMsgType() {
        return msgType;
    }

    public WebSocketMsg(){}

    public WebSocketMsg(Integer msgType, Integer showWindow, Object data){
        this.msgType = msgType;
        this.showWindow = showWindow;
        this.data = data;
    }

    public void setMsgType(Integer msgType) {
        this.msgType = msgType;
    }

    public Integer getShowWindow() {
        return showWindow;
    }

    public void setShowWindow(Integer showWindow) {
        this.showWindow = showWindow;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public List<Target2D> getList() {
        return list;
    }

    public void setList(List<Target2D> list) {
        this.list = list;
    }
}

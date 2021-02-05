package com.optical.bean;

import java.util.List;

/**
 * Created by mariry on 2019/11/15.
 */
public class WsAlarmPush {

    //websocket服务器端推送消息类型：系统消息0，报警1，其他2(待扩展)

    private List<Target2D> list;

    private Integer length;

    public List<Target2D> getList() {
        return list;
    }

    public void setList(List<Target2D> list) {
        this.list = list;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }
}

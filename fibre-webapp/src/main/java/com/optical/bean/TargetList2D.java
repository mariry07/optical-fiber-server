package com.optical.bean;

import java.util.List;

/**
 * Created by mary on 2021/1/6.
 */
public class TargetList2D {

    private Integer frameId;

    private Integer frameLength;

    private List<Target2D> list;

    public Integer getFrameId() {
        return frameId;
    }

    public void setFrameId(Integer frameId) {
        this.frameId = frameId;
    }

    public Integer getFrameLength() {
        return frameLength;
    }

    public void setFrameLength(Integer frameLength) {
        this.frameLength = frameLength;
    }

    public List<Target2D> getList() {
        return list;
    }

    public void setList(List<Target2D> list) {
        this.list = list;
    }
}

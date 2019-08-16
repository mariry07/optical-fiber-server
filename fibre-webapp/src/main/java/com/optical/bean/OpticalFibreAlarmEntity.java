package com.optical.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mariry on 2019/8/1.
 */
public class OpticalFibreAlarmEntity implements Serializable {

    private Long id;

    /*
    报警在dts终端的id
     */
    private String dtsId;

    /*
    报警源，如果是通道的报警则为通道号
     */
    private String source;

    /*
    报警码 1：系统提示 11：系统警告 21：系统故障 22：温度报警
     */
    private Integer code;

    /*
    报警类型 如常用：70：断纤报警  90：高温报警 等
    这个字段名是为了与DTS保持一致
     */
    private String level;

    /*
    收到报警时间
     */
    private Date recdTime;
    /**
     * 实际报警时间 字符串
     */
    private String alarmTimeStr;
    /**
     * 实际报警描述
     */
    private String alarmInfo;

    private Integer alarmStatus;

    /*
    逻辑删标志：0：未删除  1：已删除
     */
    private Integer df;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDtsId() {
        return dtsId;
    }

    public void setDtsId(String dtsId) {
        this.dtsId = dtsId;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getAlarmInfo() {
        return alarmInfo;
    }

    public void setAlarmInfo(String alarmInfo) {
        this.alarmInfo = alarmInfo;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }


    public Date getRecdTime() {
        return recdTime;
    }

    public void setRecdTime(Date recdTime) {
        this.recdTime = recdTime;
    }

    public Integer getAlarmStatus() {
        return alarmStatus;
    }

    public void setAlarmStatus(Integer alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    public Integer getDf() {
        return df;
    }

    public void setDf(Integer df) {
        this.df = df;
    }

    public String getAlarmTimeStr() {
        return alarmTimeStr;
    }

    public void setAlarmTimeStr(String alarmTimeStr) {
        this.alarmTimeStr = alarmTimeStr;
    }
}

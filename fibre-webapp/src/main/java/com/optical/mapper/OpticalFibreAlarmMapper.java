package com.optical.mapper;

import com.optical.bean.OpticalFibreAlarmEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by mariry on 2019/8/1.
 */
public interface OpticalFibreAlarmMapper {

    void insert(OpticalFibreAlarmEntity opticalFibreTemp);

    List<OpticalFibreAlarmEntity> getData();

    int updateAlarmStatus(Map map);

    int deleteAlarm(Map map);

    int clearAlarmAll();

}

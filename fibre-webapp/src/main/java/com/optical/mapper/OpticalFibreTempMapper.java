package com.optical.mapper;

import com.optical.bean.OpticalFibreTemp;

import java.util.List;

/**
 * Created by mariry on 2019/7/18.
 */
public interface OpticalFibreTempMapper {

    public void insert(OpticalFibreTemp opticalFibreTemp);

    public List<OpticalFibreTemp> getData();


}

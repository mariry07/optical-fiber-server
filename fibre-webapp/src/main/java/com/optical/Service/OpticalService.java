package com.optical.Service;

import org.springframework.stereotype.Component;

/**
 * Created by mariry on 2019/7/17.
 */

public interface OpticalService {

    /**
     * 接收数据总入口
     * @param request
     * @return
     */
    public byte[] DTSRequestSwitcher(byte[] request);

    /**
     * 处理dts客户端发起的客户端就绪请求，需保留通道号，采样间隔等
     * @param request
     * @return
     */
    public byte[] DTSReady(byte[] request);

    /**
     * 组装获取dts温度数据的请求串
     * @return
     */
    public byte[] getDTSTempInfo();

    /**
     * 处理dts返回的多个点的采样温度数据
     * @param request
     * @return
     */
    public byte[] handleDTSTempValue(byte[] request);



}

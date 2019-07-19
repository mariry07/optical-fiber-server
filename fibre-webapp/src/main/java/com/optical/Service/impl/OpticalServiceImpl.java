package com.optical.Service.impl;

import com.alibaba.fastjson.JSON;
import com.optical.Service.OpticalService;
import com.optical.bean.OpticalFibreTemp;
import com.optical.common.ByteUtil;
import com.optical.mapper.OpticalFibreTempMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.print.DocFlavor;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.file.OpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by mariry on 2019/7/17.
 */

@Component
@Configuration
public class OpticalServiceImpl{
    private static final Logger log = LoggerFactory.getLogger(OpticalServiceImpl.class);

    @Autowired
    private OpticalFibreTempMapper opticalFibreTempMapper;

    public byte[] DTSRequestSwitcher(byte[] request, int len) {
        log.info("here entered OpticalServiceImpl");
        /*
        1. 前3字节 11 02 00 固定
        2. 第四第五字节 方向  14  0a  :dts终端到后台
        3. 第六第七字节 数据段长度
        4. 中间数据段
        5. 倒数第4第3字节，CRC16-modbus 校验值
        6. 倒数最后2字节，11 03 结束符
         */

        byte[] startFrame = new byte[2];
        byte[] endFrame = new byte[2];
        byte[] crcFrame = new byte[2];
        byte[] dataLen = new byte[2];
        byte[] typeFrame = new byte[2];
        byte[] dataFrame = null;

        dataFrame = formateDataFrames(request, startFrame, endFrame, crcFrame, dataLen, typeFrame, len);

        if(ByteUtil.getString(typeFrame).equals("31aa")){
            //0x 31aa 读取当前温度
            if(!getDTSTempInfo(dataFrame, ByteUtil.getShort(dataLen)/4)){
                log.error("ERROR! read current temporature error! data: " + ByteUtil.getString(dataFrame));
            }
        }else if(ByteUtil.getString(typeFrame).equals("30aa")){
            // 0x 30aa DTS告知web准备就绪
            if(!DTSReady())
        }



        return new byte[0];
    }


    public Boolean DTSReady(byte[] request) {
        return true
    }

    /**
     * 保存客户端返回的当前温度
     *  类型标志：0x31， 0xaa
     *  入参 dataFrame，须已将 0x 11 0x 1a 全部替换为0x 11
     * @param dataFrame
     * @return
     */
    public Boolean getDTSTempInfo(byte[] dataFrame, Integer len) {
        List<Float> tempData;
        Float value;
        byte[] tmpByte = new byte[4];
        if(len > 500){
            log.error("每次读取的数据点数不可超过500个！");
            return false;
        }else{
            tempData = new ArrayList<>(len);
            Integer current = 0;
            while(current < len){
                System.arraycopy(dataFrame, current, tmpByte, 0, 4);

                ByteBuffer tt = ByteBuffer.wrap(tmpByte);
                value = tt.getFloat();
                tempData.add(value);
                current += 4;
            }
            //插入数据库
            OpticalFibreTemp opt = new OpticalFibreTemp();
            opt.setChannel(1);
            opt.setMsgtype(0);
            opt.setRecdTime(new Date());
            opt.setTempData(JSON.toJSONString(tempData));
            opticalFibreTempMapper.insert(opt);
            log.info("float List: " + JSON.toJSONString(tempData));
        }
        return true;
    }


    public byte[] handleDTSTempValue(byte[] request) {
        return new byte[0];
    }

    public static byte[] formateDataFrames(byte[] request, byte[] startFrame, byte[] endFrame, byte[] crcFrame,
                                         byte[] dataLen, byte[] typeFrame, int len) {
        log.info("entered formateDataFrames.request len:" + len);
        /*
        src：byte源数组
        srcPos：截取源byte数组起始位置（0位置有效）
        dest,：byte目的数组（截取后存放的数组）
        destPos：截取后存放的数组起始位置（0位置有效）
        length：截取的数据长度
         */
        System.arraycopy(request, 0, startFrame, 0, 2);
        System.arraycopy(request, 5, typeFrame, 0, 2);
        System.arraycopy(request, 10, dataLen, 0, 2);
        System.arraycopy(request, len - 4, crcFrame, 0, 2);
        System.arraycopy(request, len-2, endFrame, 0, 2);
        //删除特殊含义字符0x1a. 将 数据段的 0x11 0x1a,替换为 0x11.
        byte[] dataFrame = new byte[ByteUtil.getShort(dataLen)];
        System.arraycopy(request, 12, dataFrame, 0, ByteUtil.getShort(dataLen));

        log.info("start:" + Integer.toHexString(ByteUtil.getShort(startFrame))
                + ", end: " + Integer.toHexString(ByteUtil.getShort(endFrame))
                + ", dataLen: " + ByteUtil.getShort(dataLen)/4
                + ", type" + Integer.toHexString(ByteUtil.getShort(typeFrame))
                + ", crc:" + Integer.toHexString(ByteUtil.getShort(crcFrame))
        );
        return dataFrame;
    }


}

package com.optical.Service.impl;

import com.optical.Service.OpticalService;
import com.optical.common.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.print.DocFlavor;
import java.lang.reflect.Array;
import java.nio.file.OpenOption;

/**
 * Created by mariry on 2019/7/17.
 */

@Component
@Order(value = 1)
public class OpticalServiceImpl implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(OpticalServiceImpl.class);


    public static byte[] DTSRequestSwitcher(byte[] request, int len) {
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
        byte[] dataFrame = null;

        formateDataFrames(request, startFrame, endFrame, crcFrame, dataLen, dataFrame, len);

        return new byte[0];
    }


    public byte[] DTSReady(byte[] request) {
        return new byte[0];
    }


    public byte[] getDTSTempInfo() {
        return new byte[0];
    }


    public byte[] handleDTSTempValue(byte[] request) {
        return new byte[0];
    }

    public static void formateDataFrames(byte[] request, byte[] startFrame, byte[] endFrame, byte[] crcFrame, byte[] dataLen, byte[] dataFrame, int len) {
        log.info("entered formateDataFrames.");

        //int len = request.length;
        log.info("request len: " + len);

        /*
        src：byte源数组
        srcPos：截取源byte数组起始位置（0位置有效）
        dest,：byte目的数组（截取后存放的数组）
        destPos：截取后存放的数组起始位置（0位置有效）
        length：截取的数据长度
         */
        System.arraycopy(request, 0, startFrame, 0, 2);
        System.arraycopy(request, 10, dataLen, 0, 2);
        System.arraycopy(request, len - 4, crcFrame, 0, 2);
        System.arraycopy(request, len-2, endFrame, 0, 2);
        //删除特殊含义字符0x1a. 将 数据段的 0x11 0x1a,替换为 0x11.

        log.info("start:" + Integer.toHexString(ByteUtil.getShort(startFrame))
                + ", end: " + Integer.toHexString(ByteUtil.getShort(endFrame))
                + ", dataLen: " + ByteUtil.getShort(dataLen)/4
                + ", crc:" + Integer.toHexString(ByteUtil.getShort(crcFrame))
        );
    }

    @Override
    public void run(String... strings) throws Exception {
        log.info("init OpticalServiceImpl");
    }
}

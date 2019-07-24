package com.optical.Service.impl;

import com.alibaba.fastjson.JSON;
import com.optical.Service.OpticalService;
import com.optical.bean.OpticalFibreTemp;
import com.optical.common.ByteUtil;
import com.optical.common.CRC16Util;
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

    private static byte[] queryTemplate = {0x11, 0x02, 0x00, 0x0a, 0x14, 0x31, 0x55, 0x00, 0x06, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x11, 0x03};


    @Autowired
    private OpticalFibreTempMapper opticalFibreTempMapper;

    public byte[] DTSRequestSwitcher(byte[] request, int len) {
        /*
        初始化
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
        byte[] dataLen = new byte[2];       //数据包长度，为上传数据的第8和第9字节
        byte[] tempDataLen = new byte[2];   //温度数据长度；当上传数据包为当前温度数据时，纯温度数据长度为第11和第12字节（不包含11 02 1a的1a）
        byte[] typeFrame = new byte[2];
        byte[] tempDataFrame = null;        //当前温度数据
        byte[] queryFrame = null;          //若需要返回请求，则写入本变量

        System.arraycopy(request, 5, typeFrame, 0, 2);
        String testStr = ByteUtil.getString(request, "UTF-8");
        log.info("testStr: {}", testStr);

        if(Integer.toHexString(ByteUtil.getShort(typeFrame)).equals("31aa")){
            //0x 31aa 读取当前温度
            tempDataFrame = formateTempDataFrames(request, startFrame, endFrame, dataLen, crcFrame, tempDataLen, typeFrame, len);
            if(!saveDTSTempInfo(tempDataFrame, ByteUtil.getShort(tempDataLen)/4)){
                log.error("ERROR! read current temporature error! data: " + ByteUtil.getString(tempDataFrame));
            }
        }else if(Integer.toHexString(ByteUtil.getShort(typeFrame)).equals("30aa")){
            // 0x 30aa DTS告知web准备就绪
            // 本请求为终端定时请求，间隔与终端设置有关。
            // 收到本请求后，可向当前请求的发送端口发送获取温度信息的请求
            queryFrame = formateFetchTempratureRequest(request, 1, 0, 499, 0);
            if(queryFrame != null) {
                return queryFrame;
            }
        }else if(Integer.toHexString(ByteUtil.getShort(typeFrame)).equals("3caa")) {
            //DTS主动推送报警信息
            int alarmType = ByteUtil.getInt(request[10]);
            if(alarmType == 3) {
                //TODO: 清空报警
                log.info("收到清空报警信息通知， 时间： " + new Date());

            }else if(alarmType == 1){
                //TODO:添加报警
                //testStr: 1通道1实际接入的光纤长度超限：当前测量长度494.0米，实际接入光纤长度2414.8米。1_au002通道1-６号柜中高温报警：5.3-11.0;

            }else if(alarmType == 2) {
                //删除报警,本报警类型已经废弃，不做处理
            }

        }
        log.error("ERROR: DTSRequestSwitcher: unknown request type: " + Integer.toHexString(ByteUtil.getShort(typeFrame)));

        return null;
    }


    /**
     * 保存客户端返回的当前温度
     *  类型标志：0x31， 0xaa
     *  入参 dataFrame，须已将 0x 11 0x 1a 全部替换为0x 11
     * @param dataFrame
     * @return
     */
    public Boolean saveDTSTempInfo(byte[] dataFrame, Integer len) {
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


    /**
     * 处理 0x 31aa ，即读取当前温度的返回值
     * 将每个点的温度数据组成float数组，存入数据库
     * @param request
     * @param startFrame
     * @param endFrame
     * @param wholeDataLen
     * @param crcFrame
     * @param dataLen
     * @param typeFrame
     * @param len
     * @return
     */
    public byte[] formateTempDataFrames(byte[] request, byte[] startFrame, byte[] endFrame,byte[] wholeDataLen, byte[] crcFrame,
                                         byte[] dataLen, byte[] typeFrame, int len) {
        /*
        src：byte源数组
        srcPos：截取源byte数组起始位置（0位置有效）
        dest,：byte目的数组（截取后存放的数组）
        destPos：截取后存放的数组起始位置（0位置有效）
        length：截取的数据长度
         */
        System.arraycopy(request, 0, startFrame, 0, 2);
        System.arraycopy(request, 7, wholeDataLen, 0, 2);
        System.arraycopy(request, 10, dataLen, 0, 2);
        System.arraycopy(request, len - 4, crcFrame, 0, 2);
        System.arraycopy(request, len-2, endFrame, 0, 2);
        //删除特殊含义字符0x1a. 将 数据段的 0x11 0x1a,替换为 0x11.
        byte[] dataFrame = new byte[ByteUtil.getShort(dataLen)];
        System.arraycopy(request, 12, dataFrame, 0, ByteUtil.getShort(dataLen));

        /*
        log.info("start:" + Integer.toHexString(ByteUtil.getShort(startFrame))
                + ", end: " + Integer.toHexString(ByteUtil.getShort(endFrame))
                + ", wholeDateLen: " + ByteUtil.getShort(wholeDataLen)
                + ", dataLen: " + ByteUtil.getShort(dataLen)/4
                + ", type" + Integer.toHexString(ByteUtil.getShort(typeFrame))
                + ", crc:" + Integer.toHexString(ByteUtil.getShort(crcFrame))
        );
       */
        return dataFrame;
    }

    /**
     *
     * @param request
     * @param channelNo 通道号
     * @param start     开始点位
     * @param end       结束点位
     * @param dataType  要获取的数据类型  0：温度， 1：pa，  2：ps
     * @return
     */
    public byte[] formateFetchTempratureRequest(byte[] request, Integer channelNo, Integer start, Integer end, Integer dataType) {
        //组装获取温度信息的请求,具体发送含义如下：
        //PC->DTS：PC回复要读取温度（命令码0x3155），通道号1，温度数据索引为0-499
        //11 02 00 0a 14
        // 31 55    任务码
        // 00 06    数据字段长度
        // 01       通道号
        // 00 00    起始点位
        // 01 f3    结束点位
        // 00       数据类型 00:温度,  01:pa,  02:ps
        // dd 62    CRC校验帧
        // 11 03    结束帧

        //TODO：本请求总长19，暂时hardcode，后续加静态常量
        byte[] query = new byte[19];
        System.arraycopy(queryTemplate, 0, query, 0, 19);
        //设置通道号
        query[9] = ByteUtil.getByte(channelNo);
        //设置起点
        System.arraycopy(ByteUtil.getBytes(start), 0, query, 10, 2);
        //设置终点
        System.arraycopy(ByteUtil.getBytesByIntAsShort(end), 0, query, 12, 2);
        //设置数据类型
        query[14] = ByteUtil.getByte(dataType);
        //计算并赋值CRC校验帧
        byte[] crcData = new byte[12];
        System.arraycopy(query, 3, crcData, 0, 12); //本请求 需crc校验的帧长度为12，hardcode
        byte[]crcByte = ByteUtil.getCRCBytesByIntAsShort(CRC16Util.calcCrc16(crcData));
        System.arraycopy(crcByte, 0, query, 15, 2);
//        printLog(query);
        return query;
    }

    /**
     * 将字节流按照16进制字符串打印出来
     * @param info
     */
    public void printLog(byte[] info) {
        StringBuffer sb = new StringBuffer();
        byte[] tempstr = new byte[2];
        tempstr[1] = 0;
        for(int i = 0; i< 19; i++) {
            System.arraycopy(info, i, tempstr, 0, 1);
            sb.append(Integer.toHexString(ByteUtil.getTiny(tempstr)));
        }
        log.info("hex string: " + sb.toString());
    }


}

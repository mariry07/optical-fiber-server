package com.optical.component;

import com.alibaba.fastjson.JSON;
import com.optical.bean.Target2D;
import com.optical.bean.TargetList2D;
import com.optical.bean.WebSocketMsg;
import com.optical.bean.WsAlarmPush;
import com.optical.common.ByteUtil;
import com.optical.common.EncodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Created by mary on 2021/1/6.
 */
public class MsgConsumer implements Runnable{

    private static final Logger log = LoggerFactory.getLogger(MsgConsumer.class);

    private final BlockingQueue<String> queue;

    public MsgConsumer(BlockingQueue q)
    {
        this.queue = q;
    }

    @Autowired
    private MyWebsocketServer websocketServer;

    @Override
    public void run() {
        try{
            int count = 0;
            while (true) {
                try{
                    log.debug("queue length: " + queue.size());
                    consume(queue.take(), count);
                }catch (InterruptedException e){
                    log.error("consume Exception! e = {}", e);
                }
            }
        }catch (Exception e) {
            log.error("Exception! e={}", e);
            return;
        }
    }


    private String consume(String msg, int count) {

        decodeMsg(msg);
        return "success";
    }

    private boolean decodeMsg(String msg) {
        boolean b = true;
        try{
            //仅识别0xA0 即2D目标检测
            //1. HexString 转byte[]
            byte[] byteMsg = EncodeUtil.hexStringToBytes(msg);
            byte type;
//            log.info("byteArr length: " + byteMsg.length);

            if(ByteUtil.getUnsignedInt(byteMsg[4]) != 0xa0) {
                //非2D目标检测帧，不做处理
                log.debug("类型：" + byteMsg[4] + ", 不错处理！");
            }else {
                TargetList2D targetList2D = new TargetList2D();
                //2. 获取帧长，减掉12后，得到数据包长度（小端模式),并截取
                byte[] tmpByte = new byte[4];
                System.arraycopy(byteMsg, 0, tmpByte, 0, 4);
                Integer frameLen = ByteUtil.getIntLowEndian(tmpByte);
                targetList2D.setFrameLength(frameLen);
//                log.info("frameLen: " + frameLen);
                //获取帧号
                System.arraycopy(byteMsg, 5, tmpByte, 0, 4);
                Integer frameNo = ByteUtil.getIntLowEndian(tmpByte);
                //帧号：小端模式
                targetList2D.setFrameId(frameNo);

                //计算本帧数据中 总识别目标数, 处理数据包部分，提炼出targetList2D
                Integer targetCount = (frameLen - 16) / 25;
                List<Target2D> list = new ArrayList(targetCount);
                int current = 0;
                while(current < targetCount) {

                    Target2D tgt = new Target2D();
                    // 要减掉被split替换没的起始帧，有点丑陋 9 = 13 - 4
                    tgt.setTargetId(ByteUtil.getUnsignedInt(byteMsg[9 + 25 * current]));
                    //x方向位置 14-4
                    System.arraycopy(byteMsg, 10 + 25 * current, tmpByte, 0, 4);
                    tgt.setTargetX((double)ByteUtil.getLowFloat(tmpByte));
                    //y方向位置 18 - 4

                    System.arraycopy(byteMsg, 14 + 25 * current, tmpByte, 0, 4);
                    tgt.setTargetY((double)ByteUtil.getLowFloat(tmpByte));
                    //x方向速度 22-4
                    System.arraycopy(byteMsg, 18 + 25 * current, tmpByte, 0, 4);
                    tgt.setxSpeed((double)ByteUtil.getLowFloat(tmpByte));
                    //y方向速度 26-4
                    System.arraycopy(byteMsg, 22 + 25 * current, tmpByte, 0, 4);
                    tgt.setySpeed((double)ByteUtil.getLowFloat(tmpByte));
                    //x方向加速度 30-4
                    System.arraycopy(byteMsg, 26 + 25 * current, tmpByte, 0, 4);
                    tgt.setxAccSpeed((double)ByteUtil.getLowFloat(tmpByte));
                    //x方向加速度 34-4
                    System.arraycopy(byteMsg, 30 + 25 * current, tmpByte, 0, 4);
                    tgt.setxAccSpeed((double)ByteUtil.getLowFloat(tmpByte));

                    current++;
                    list.add(tgt);
                }
                //list 按照id升序排序 方便前端展示
                list.sort((x, y) -> x.getTargetId() - y.getTargetId());
                log.info("list: " + JSON.toJSONString(list));
                targetList2D.setList(list);
                log.debug("处理完成一个2D 目标识别消息： 结果list：" + JSON.toJSONString(targetList2D));

                //4. websocket推送前端，由前端进行3D/2D渲染
//                WsAlarmPush wAlarm = new WsAlarmPush();
//                wAlarm.setLength(list.size());
//                wAlarm.setList(list);
                WebSocketMsg wsmsg = new WebSocketMsg();
                wsmsg.setMsgType(1);
                wsmsg.setLength(list.size());
                wsmsg.setList(list);

//                wsmsg.setData(wAlarm);
                websocketServer.sendInfo(JSON.toJSONString(wsmsg), null);

            }


        }catch (Exception e) {
            log.error("Exception! e = {}", e);
            b = false;
            return b;
        }

        return b;
    }


    public static float getFloat(String msg) {
        byte[] byteMsg = EncodeUtil.hexStringToBytes(msg);
        int accum = 0;
        byte[] b = new byte[4];
        System.arraycopy(byteMsg, 10, b, 0, 4);
        System.out.println("x distence: " + EncodeUtil.binary2Hex(b));
        accum = accum|(b[3] & 0xff) << 0;
        accum = accum|(b[2] & 0xff) << 8;
        accum = accum|(b[1] & 0xff) << 16;
        accum = accum|(b[0] & 0xff) << 24;

        ByteBuffer buf= ByteBuffer.allocateDirect(4); //无额外内存的直接缓存
//buf=buf.order(ByteOrder.LITTLE_ENDIAN);//默认大端，小端用这行
        buf.put(b);
        buf.rewind();
        float f2=buf.getFloat();
        System.out.println(accum);
        return Float.intBitsToFloat(accum);
    }

    public void main(String[] args) {
        String hexStr = "42000000a08712000001ce0b68bfe1f39a3f958ddcbe2ec3f4bbae9b933e807f353e0044a1b6bfd28e8740a5295abe449365be28ff64bbb4eea1be903f28";

//        getFloat(hexStr);

//        decodeMsg(hexStr);


    }
}

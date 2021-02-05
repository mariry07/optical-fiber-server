package com.optical.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.io.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by mary on 2021/1/5.
 */
public class LoadLocalDat implements Runnable{


    private static final Logger log = LoggerFactory.getLogger(LoadLocalDat.class);

    private final BlockingQueue<String> list;

    //队列大小，先这么丑陋的写一下
    private final int maxSize;


    public LoadLocalDat(BlockingQueue list, int maxSize) {
        this.list = list;
        this.maxSize = maxSize;
    }

    private String produceMsg(String msg, Integer count) {
        synchronized (list) {
            if(list.size() == maxSize) {
                log.error("缓冲区已满！新消息丢弃！ new msg = {}", msg);
            }else {
                count++;
                list.add(msg);
                log.info("here queue size: " + list.size());
                //假设未来会有多个消费者，先notifyAll
//                list.notifyAll();
            }
        }
        return "";
    }

    private static void write2File(String data, String localFilePath) throws FileNotFoundException {

        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(localFilePath, true)));
            out.write(data);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//            File file =new File("d:\\000hexStrResult.txt");
//            FileOutputStream fos=new FileOutputStream(file);
//            fos.write(data);
//            fos.close();
//            return;
//        }catch (Exception e) {
//            System.out.println("write Error!");
//            return;
//        }
    }

    private void getDatFile() throws IOException {
        FileInputStream fis = null;
        Integer infoSent = 0;

        try {
            fis = new FileInputStream("D:\\ti_send.dat");
            byte[] buf = new byte[2048];
            String hexString = "";
            String unionStr = "";
            String currentInfoStr = "";
            byte[] infoBuf;
            String[] infoArr;
            while (true) {
                int count = 0;
                try{
                    int len = 0;
                    //每次读进来这么buf.length个字节到buf中去
                    while((len = fis.read(buf)) != -1){
                        System.out.println(" read in len : " + len);
                        infoBuf = new byte[len];
                        // 去掉不足长数据包结尾的空格0
                        System.arraycopy(buf, 0, infoBuf, 0, len);
                        hexString = EncodeUtil.binary2Hex(infoBuf);
                        //待处理数据包infoBuf
                        //1. 寻找开头帧0x4c795261
                        //需要考虑lyra头被拆分到上下两个包，在每个单独包中不完整，但聚合之后变完整的情况，
                        // 所以需要与currentInfoStr合并之后统一做一次拆分
                        unionStr = currentInfoStr + hexString;
                        if(!unionStr.contains("4c795261")) {
                            //当前不包含帧开头字节符，应全部追加到currentInfoStr中
                            //检查拼接后的currentInfoStr是否包含lary头
                            currentInfoStr = unionStr;

                        }else {
                            //找到第一个lyra头，并舍弃之前的部分
                            Integer firstLyra = unionStr.indexOf("4c795261");
                            if(firstLyra == -1 ){
                                continue;
                            }
                            if(firstLyra != 0) {
                                //开头不是lyra,则第一个lyra之前的部分全部舍弃掉
                                unionStr = unionStr.substring(firstLyra);

                            }
                            infoArr = unionStr.split("4c795261");
                            //包含数据帧开头至少一个，则第一个数组应与currentInfoStr合并后发送
                            //中间数组单独发送
                            //最后一个数组元素需判断是否是一个完整消息(根据长度)：
                            //若是，则currentInfoStr清空；若不是，则currentInfoStr替换为最后一个数组元素
                            for(int i = 0; i < infoArr.length; i++) {
//                                log.info("i = " + i);

                                if(i < infoArr.length -1 ) {
                                    //去掉分隔符在开头或结尾导致split来的空数组
                                    if(StringUtils.isEmpty(infoArr[i])) {
                                        log.info("empty str, continue");
                                        continue;
                                    }
                                    //TODO: 处理数据，或者扔队列
                                    handleSingleInfo(infoArr[i], count);
                                    infoSent++;
                                }else {
                                    //最后一个数据包，暂时不做处理;因为可以保证是lyra开头的，所以追加上lyra头之后，和下一次接收到的数据拼装到一起处理
                                    currentInfoStr = "4c795261" + infoArr[i];

                                    // 需要检查长度信息，以便决定是直接发送并清空currentInfoStr还是保留
//
//                                    //先判断帧长数据是否给完整4字节
//                                    if(infoArr[i].length() < 4) {
//                                        //判断是否是局部的lyra头
//
//                                        //说明数据长度不完整，直接添加lyra头，然后保留到currentInfoStr中
//                                        currentInfoStr = "4c795261" + infoArr[i];
//                                    }else{
//                                        //小端模式计算长度：
//                                        //最低位：
//                                        String tmp = infoArr[i];
//                                        //byte转hex字符串后长度变为原来的2倍
//                                        Integer totalLen = (Integer.parseInt(tmp.substring(0,1), 16) * 16
//                                                + Integer.parseInt(tmp.substring(1,2), 16)
//                                                + Integer.parseInt(tmp.substring(2,3), 16) * 4096
//                                                + Integer.parseInt(tmp.substring(3,4), 16) * 256) * 2
//                                                //扣除已经被替换掉的开头帧0x 4c795261
//                                                - 8;
//                                        log.info("totalLen: " + totalLen);
//
//                                        if(tmp.length() == totalLen){
//                                            //是一个完整的数据包，直接发送
//                                            handleSingleInfo(tmp);
//                                            log.info("perfect match, sent!");
//                                            currentInfoStr = "";
//                                        }else {
//                                            currentInfoStr = tmp;
//                                        }
//                                    }
                                }
                            }
                        }

//                        hexString = EncodeUtil.binary2Hex(infoBuf);
                        //16进制数据 写文件
//                        write2File(hexString, "d:\\000hexStrResult.txt");
//                        System.out.println(hexString);
                    }
//                    obj = ois.readObject();
//                    list.add(obj);
                }catch(Exception e){
                    break;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("找不到指定文件");
            System.exit(-1);
        }catch (Exception e) {
            e.printStackTrace();
        }finally{
            System.out.println("ended here");
            if(fis!=null){
                fis.close();
            }
        }
        return ;
    }


    private void handleSingleInfo(String info, Integer count) {
        produceMsg(info, count);
        log.info("message received: " + info);
        return;
    }

    public static void main(String[] args) throws IOException {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(100);
//        getDatFile();
    }

    @Override
    public void run() {
        try{
            getDatFile();
        }catch (Exception e) {
            log.error("Exception! e={}", e);
        }
    }
}

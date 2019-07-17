package com.optical.component;

import com.optical.Service.OpticalService;
import com.optical.Service.impl.OpticalServiceImpl;
import com.optical.common.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.DataFormatException;

/**
 * Created by mariry on 2019/7/16.
 */

public class ServerConfig extends Thread{

    private static final Logger log = LoggerFactory.getLogger(ServerConfig.class);

    //@Autowired
    //private OpticalService opticalService;

    private Socket socket;

    public ServerConfig(){}

    public ServerConfig(Socket socket) {
        this.socket = socket;
    }

    private String handle(InputStream is) throws Exception {

        byte[] inBytes = new byte[10000];
        int len = is.read(inBytes);
        if(len != -1) {
            StringBuffer request = new StringBuffer();
            request.append(new String(inBytes, 0, len, "UTF-8"));
            log.info("request: " + ByteUtil.getString(inBytes));
            log.info("request: " + request.toString());
            //TODO: 依次解码，按照类型处理16进制数据
            OpticalServiceImpl.DTSRequestSwitcher(inBytes, len);

            return "ok";
        }else{
            log.error("socket数据读取处理异常");
            throw new DataFormatException("数据处理异常");
        }
    }
    @Override
    public void run() {

        BufferedWriter writer = null;
        try {
            // 设置连接超时9秒
            socket.setSoTimeout(9000);
            log.info(socket.getRemoteSocketAddress() + " -> 连接成功");
            InputStream inputStream = socket.getInputStream();
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            String result = null;
            try {
                result = handle(inputStream);
                log.info("result: " + result);
                writer.write(result);
                writer.newLine();
                writer.flush();
            } catch (Exception e) {
                writer.write("error");
                writer.newLine();
                writer.flush();
                log.error("发生异常! e: " + e);
                try {
                    log.error("再次接受!");
                    result = handle(inputStream);
                    writer.write(result);
                    writer.newLine();
                    writer.flush();
                } catch (Exception e1) {
                    log.error("再次接受, 发生异常,连接关闭, e1: " + e1);
                }
            }
        } catch (SocketException socketException) {
            socketException.printStackTrace();
            try {
                writer.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



}

package com.optical.component;

import com.optical.Service.OpticalService;
import com.optical.Service.impl.OpticalServiceImpl;
import com.optical.common.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.zip.DataFormatException;

/**
 * Created by mariry on 2019/7/16.
 */
@Service
public class ServerConfig extends Thread{

    private static final Logger log = LoggerFactory.getLogger(ServerConfig.class);

    private Socket socket;

    private OpticalServiceImpl opticalService;

    public ServerConfig(){}

    public ServerConfig(Socket socket) {
        this.socket = socket;
    }

    public ServerConfig(Socket socket, OpticalServiceImpl opticalService) {
        this.socket = socket;
        this.opticalService = opticalService;
    }

    private byte[] handle(InputStream is) throws Exception {

        byte[] inBytes = new byte[10000];
        int len = is.read(inBytes);
        if(len != -1) {
            StringBuffer request = new StringBuffer();
            request.append(new String(inBytes, 0, len, "UTF-8"));
            return opticalService.DTSRequestSwitcher(inBytes, len);
        }else{
            log.error("socket数据读取处理异常");
            throw new DataFormatException("数据处理异常");
        }
    }
    @Override
    public void run() {

        BufferedWriter writer = null;
        DataOutputStream output = null;

        try {
            // 设置连接超时15秒
            socket.setSoTimeout(15000);
            log.info(socket.getRemoteSocketAddress() + " -> 连接成功");
            InputStream inputStream = socket.getInputStream();
            output = new DataOutputStream(socket.getOutputStream());
//            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            byte[] result = null;
            try {
                result = handle(inputStream);
                output.write(result, 0, result.length);
                output.flush();
//                writer.write(ByteUtil.getString(result));
//                writer.newLine();
//                writer.flush();
            } catch (Exception e) {
                output.writeBytes("error");
                output.flush();
//                writer.write("error");
//                writer.newLine();
//                writer.flush();
                log.error("发生异常! e: " + e);
                try {
                    log.error("再次接受!");
                    result = handle(inputStream);
                    output.write(result, 0, result.length);
                    output.flush();
//                    writer.write(result.toString());
//                    writer.newLine();
//                    writer.flush();
                } catch (Exception e1) {
                    log.error("再次接受, 发生异常,连接关闭, e1: " + e1);
                }
            }
        } catch (SocketException socketException) {
            socketException.printStackTrace();
            try {
                output.close();
//                writer.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
//        finally {
//            try {
//                output.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
    }

}

package com.optical.component;

import com.alibaba.fastjson.JSON;
import com.optical.bean.WebSocketMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author Gjing
 **/
@ServerEndpoint("/targetlist/{sid}")
@Component
public class MyWebsocketServer {

    private static final Logger log = LoggerFactory.getLogger(MyWebsocketServer.class);



    //记录当前在线人数
    private static int onlineCount = 0;
    //记录累计登陆量
    private static int visitCount = 0;


    //线程安全set，保存当前每个登录用户的webSocketService 对象
    private static CopyOnWriteArraySet<MyWebsocketServer> webSocketSet = new CopyOnWriteArraySet<MyWebsocketServer>();

    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;

    //接收sid
    private String sid="";


    @OnOpen
    public void onOpen(Session session,@PathParam("sid") String sid) {
        this.session = session;
        webSocketSet.add(this);     //加入set中
        addOnlineCount();           //在线数加1
        log.info("有新窗口开始监听:"+sid+",当前在线人数为" + getOnlineCount());
        this.sid=sid;
        try {
            WebSocketMsg msg = new WebSocketMsg();
            Map map = new HashMap();
            map.put("msgType", 0);
            map.put("text", "连接成功");

            sendMessage(JSON.toJSONString(msg));
        } catch (Exception e) {
            log.error("websocket IO异常");
        }
    }


    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose() {
        webSocketSet.remove(this);  //从set中删除
        subOnlineCount();           //在线数减1
        log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息*/
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到来自窗口"+sid+"的信息:"+message);
        //群发消息
//        for (WebSocketService item : webSocketSet) {
//            try {
//                item.sendMessage(JSON.toJSONString(message));
//                if(item.sid.equals(sid)){
//                    Map map = new HashMap();
//                    map.put("msgType", 0);
//                    map.put("text", message);
//
//                }
//            } catch (Exception e) {
//                log.error("websocket service Error! e = {}", e);
//            }
//        }
    }

    /**
     *
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误! Exception! error! e={}", error);
        log.error("相应session: {}", JSON.toJSONString(session));
    }


    /**
     * 群发自定义消息
     * */
    public static void sendInfo(String message,@PathParam("sid") String sid) throws Exception {
        log.info("推送消息到窗口"+sid+"，推送内容:"+message);
        for (MyWebsocketServer item : webSocketSet) {
            try {
                //这里可以设定只推送给这个sid的，为null则全部推送
                if(sid==null) {
                    item.sendMessage(message);
                }else if(item.sid.equals(sid)){
                    item.sendMessage(message);
                }
            } catch (Exception e) {
                log.error("websocketService sendInfo exception! e={}", e);
                continue;
            }
        }
    }


    /**
     * 实现服务器主动推送
     */
    public void sendMessage(String message) throws Exception {
        this.session.getBasicRemote().sendText(message);

    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public static synchronized int getVisitCount() {
        return visitCount;
    }


    public static synchronized void addOnlineCount() {
        MyWebsocketServer.onlineCount++;
        MyWebsocketServer.visitCount++;
    }

    public static synchronized void subOnlineCount() {
        MyWebsocketServer.onlineCount--;
    }


//
//    /**
//     * 存放所有在线的客户端
//     */
//    private static Map<String, Session> clients = new ConcurrentHashMap<>();
////    private JSON gson = new JSON();
//
//    @OnOpen
//    public void onOpen(Session session) {
//        log.info("有新的客户端连接了, sessionId: {}", session.getId());
//        //将新用户存入在线的组
//        clients.put(session.getId(), session);
//    }
//
//    /**
//     * 客户端关闭
//     * @param session session
//     */
//    @OnClose
//    public void onClose(Session session) {
//        log.info("有用户断开了, session id为:{}", session.getId());
//        //将掉线的用户移除在线的组里
//        clients.remove(session.getId());
//    }
//
//    /**
//     * 发生错误
//     * @param throwable e
//     */
//    @OnError
//    public void onError(Throwable throwable) {
//        throwable.printStackTrace();
//    }
//
//
//    /**
//     * 收到客户端发来消息
//     * @param message  消息对象
//     */
//    @OnMessage
//    public void onMessage(String message) {
//        log.info("服务端收到客户端发来的消息: {}", message);
//        this.sendAll(message);
//    }
//
//
//    /**
//     * 群发消息
//     * @param message 消息内容
//     */
//    private void sendAll(String message) {
//        for (Map.Entry<String, Session> sessionEntry : clients.entrySet()) {
//            sessionEntry.getValue().getAsyncRemote().sendText(message);
//        }
//    }

}

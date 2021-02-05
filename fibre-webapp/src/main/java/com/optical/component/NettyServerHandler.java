package com.optical.component;

import com.optical.common.EncodeUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 *
 * netty服务端处理器
 **/
@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {
    private final BlockingQueue<String> list;
    //队列大小，先这么丑陋的写一下
    private final int maxSize;
    public NettyServerHandler(BlockingQueue list, int maxSize) {
        this.list = list;
        this.maxSize = maxSize;
    }

    /**
     * 客户端连接会触发
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        //TODO: 完善hashMap
        //往channel map中添加channel信息
        NettyServer.getMap().put(getIPString(ctx), ctx.channel());
        //往channel 的messageMap 中放入currentString,并初始化为
        NettyServer.getMessageMap().put(getIPString(ctx), "");

        //TODO: 初始化一个针对当前channel的consumer，只消费当前channel数据；初始化时带入当前map的key值
        //启动消费者线程
        MsgConsumer c = new MsgConsumer(list);
        Thread consumer = new Thread(c);
        consumer.setName("消费者"+ getIPString(ctx));
        consumer.start();


        log.info("Channel active......");
    }

    /**
     * 客户端发消息会触发
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        //按照16进制发送时，在ServiceChannelInitializer类里需取消自动按照UTF-8来decode，以防止出现乱码
        ByteBuf buf = (ByteBuf) msg;
        byte[] receiveMsgBytes = new byte[buf.readableBytes()];
        buf.readBytes(receiveMsgBytes);

        log.info("服务器收到消息: " + EncodeUtil.binary2Hex(receiveMsgBytes));
        //消息处理后扔给队列
        getDatFile(receiveMsgBytes, getIPString(ctx));

        ctx.write("server received your msg :)");
        ctx.flush();
    }


    /**
     * 心跳机制  用户事件触发
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        if (evt instanceof IdleStateEvent)
        {
            IdleStateEvent e = (IdleStateEvent) evt;

            //检测 是否 这段时间没有和服务器联系
            if (e.state() == IdleState.ALL_IDLE)
            {
                //TODO: 检测心跳
//                checkIdle(ctx);
            }
        }
        super.userEventTriggered(ctx, evt);
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        TODO: 删除Channel Map中的失效Client
        NettyServer.getMap().remove(getIPString(ctx));
        NettyServer.getMessageMap().remove(getIPString(ctx));
        ctx.close();
        //TODO: 同步杀掉进程

    }

    /**
     * 发生异常触发
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();

        //TODO: 同步杀掉进程

    }

    public static String getIPString(ChannelHandlerContext ctx){
        String ipString = "";
        String socketString = ctx.channel().remoteAddress().toString();
        int colonAt = socketString.indexOf(":");
        ipString = socketString.substring(1, colonAt);
        return ipString;
    }

    private String produceMsg(String msg) {
        synchronized (list) {
            if(list.size() == maxSize) {
                log.error("缓冲区已满！新消息丢弃！ new msg = {}", msg);
            }else {
                list.add(msg);
                log.info("here queue size: " + list.size());
            }
        }
        return "";
    }
    private void handleSingleInfo(String info) {
        produceMsg(info);
        log.info("message received: " + info);
        return;
    }

    private void getDatFile(byte[] infoStr, String channelMapKey) throws IOException{
        byte[] infoBuf;
        String hexString = "";
        String unionStr = "";

        String[] infoArr;
        String currentInfoStr = NettyServer.getMessageMap().get(channelMapKey);
        try{
            hexString = EncodeUtil.binary2Hex(infoStr);
            //待处理数据包infoBuf
            //1. 寻找开头帧0x4c795261
            //需要考虑lyra头被拆分到上下两个包，在每个单独包中不完整，但聚合之后变完整的情况，
            // 所以需要与currentInfoStr合并之后统一做一次拆分
            unionStr = currentInfoStr + hexString;
            if(!unionStr.contains("4c795261")) {
                //当前不包含帧开头字节符，应全部追加到currentInfoStr中
                //检查拼接后的currentInfoStr是否包含lary头
                currentInfoStr = unionStr;
                NettyServer.getMessageMap().put(channelMapKey, currentInfoStr);
            }else {
                //找到第一个lyra头，并舍弃之前的部分
                Integer firstLyra = unionStr.indexOf("4c795261");
                if(firstLyra == -1 ){
                    // do nothing
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
                        handleSingleInfo(infoArr[i]);
                    }else {
                        //最后一个数据包，暂时不做处理;因为可以保证是lyra开头的，所以追加上lyra头之后，和下一次接收到的数据拼装到一起处理
                        currentInfoStr = "4c795261" + infoArr[i];
                        NettyServer.getMessageMap().put(channelMapKey, currentInfoStr);
                        log.info("currentStr in map: " + NettyServer.getMessageMap().get(channelMapKey));
                    }
                }
            }
        }catch (Exception e) {
            log.info("Error! e = {}", e);
            return;
        }
    }

}

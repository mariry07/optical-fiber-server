package com.optical.common;

import com.optical.component.MsgConsumer;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by mary on 2021/1/6.
 */
public class TestMain {

    public static void main(String[] args)
    {
        BlockingQueue<Integer> queue = new ArrayBlockingQueue<>(100);
        LoadLocalDat p = new LoadLocalDat(queue, 100);
        MsgConsumer c = new MsgConsumer(queue);

        Thread producer = new Thread(p);
        producer.setName("生产者");
        Thread consumer = new Thread(c);
        consumer.setName("消费者");

        producer.start();
        consumer.start();
    }
}

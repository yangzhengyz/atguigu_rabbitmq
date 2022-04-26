package com.atguigu.rabbitmq.four;

import com.atguigu.rabbitmq.utils.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * “生活本来很枯燥，但跑起来就有风”
 *
 * @author 杨正
 * @Package com.atguigu.rabbitmq.four
 * @date 2022/4/9 17:56
 * 发布确认模式  使用的时间 比较哪个方式好
 * 1.单个确认
 * 2.批量确认
 * 3.异步确认发布
 */
public class ConfirmMessage {
    //    批量发消息的个数
    public static final int MESSAGE_COUNT=1000;
    public static void main(String[] args) throws Exception {
//        1.单个确认 588ms
//        publishMessageIndividually();
//        2.批量确认 116ms
//        publishMessageBatch();
//        3.异步确认发布 56ms
        publishMessageAsync();
    }
    public static void publishMessageIndividually() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
//        开启发布确认
        channel.confirmSelect();
//        开始时间
        long begin = System.currentTimeMillis();
//        批量发消息
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = i+"";
            channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
//            发布确认
            boolean falg = channel.waitForConfirms();
            if (falg) {
                System.out.println("消息发送成功");
            }
        }
//        结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "个单独确认消息，消耗" + (end - begin) + "ms");
    }

    public static void publishMessageBatch() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
//        开启发布确认
        channel.confirmSelect();
//        开始时间
        long begin = System.currentTimeMillis();

//        批量确认大小
        int batch = 100;
//        批量发布消息，批量确认
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = i+"";
            channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
            if ((i + 1) % batch == 0) {
//            发布确认
                channel.confirmSelect();
            }
        }

//        结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "批量确认消息，消耗" + (end - begin) + "ms");
    }

    public static void publishMessageAsync() throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
        String queueName = UUID.randomUUID().toString();
        channel.queueDeclare(queueName, true, false, false, null);
//        开启发布确认
        channel.confirmSelect();

        /*
        * 线程安全的有序的一个哈希表，适用于高并发的情况下
        *1.轻松的将序号与消息进行关联
        *2.轻松批量删除条目 只要给到序列号
        *3.支持并发访问
        * */
        ConcurrentSkipListMap<Long, String> outstandingConfirms = new ConcurrentSkipListMap<Long,String>();

//        消息确认成功 回调函数
        /*
         * 确认收到消息的一个回调
         * 1.消息序列号
         * 2.true 可以确认小于等于当前序列号的消息
         * false 确认当前序列号消息
         */
        ConfirmCallback ackCallback=(deliveryTag,multiple)->{
            if (multiple) {
//            2.删除已经确认的消息 剩下的就是未确认的
                ConcurrentNavigableMap<Long, String> confirmed = outstandingConfirms.headMap(deliveryTag);
                confirmed.clear();
            } else {
                outstandingConfirms.remove(deliveryTag);
            }
            System.out.println("确认的消息"+deliveryTag);
        };
//        消息确认失败 回调函数
        ConfirmCallback nackCallback=(deliveryTag,multiple)->{
//            3.打印一下未确认的消息有哪些
            String message = outstandingConfirms.get(deliveryTag);
            System.out.println("未确认的消息"+message+"：：：tag为"+deliveryTag);
        };

//        准备消息的监听器，监听哪些成功了，哪些失败了
        /*
         * 添加一个异步确认的监听器
         * 1.确认收到消息的回调
         * 2.未收到消息的回调
         */
        channel.addConfirmListener(ackCallback,nackCallback);
//        开始时间
        long begin = System.currentTimeMillis();

        for (int i = 0; i < MESSAGE_COUNT; i++) {
            String message = i+"";
//            1.记录下所有发送的消息
            outstandingConfirms.put(channel.getNextPublishSeqNo(), message);
            channel.basicPublish("", queueName, null, message.getBytes(StandardCharsets.UTF_8));
        }

        //        结束时间
        long end = System.currentTimeMillis();
        System.out.println("发布" + MESSAGE_COUNT + "批量确认消息，消耗" + (end - begin) + "ms");
    }
}

package com.atguigu.rabbitmq.five;

import com.atguigu.rabbitmq.utils.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * “生活本来很枯燥，但跑起来就有风”
 *
 * @author 杨正
 * @Package com.atguigu.rabbitmq.five
 * @date 2022/4/10 17:37
 * 负责消息的接受
 */
public class ReceiveLogs01 {
    //    交换机名称
    public static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
//        声明一个交换机
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
//        声明一个队列，临时队列 ，队列的名称是随机的，当消费者断开与队列的链接的时候，队列就自动删除
        String queueName = channel.queueDeclare().getQueue();
//        绑定交换机与队列
        channel.queueBind(queueName, EXCHANGE_NAME, "111");
        System.out.println("等待接收消息，把接收到到的消息打印在屏幕上");

//        接收消息
        DeliverCallback deliverCallback=(consumerTag,message)->{
            System.out.println("ReceiveLogs01控制台打印接收到的消息：" + new String(message.getBody()));
        };

        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});

    }
}

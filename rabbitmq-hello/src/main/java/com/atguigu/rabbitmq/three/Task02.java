package com.atguigu.rabbitmq.three;

import com.atguigu.rabbitmq.utils.RabbitMqUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

import java.lang.management.ManagementPermission;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * “生活本来很枯燥，但跑起来就有风”
 *
 * @author 杨正
 * @Package com.atguigu.rabbitmq
 * @date 2022/4/9 12:42
 * 消息再手动应答时不丢失，放回队列中重新消费
 */
public class Task02 {
    public static final String TASK_QUEUE_NAME = "ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
//        声明队列
//        队列持久化
        boolean durable = true;
        channel.queueDeclare(TASK_QUEUE_NAME, durable, false, false, null);
//        从控制台中输入信息
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String message = scanner.next();
//            要求生产者发送消息为持久化消息（要求保存到磁盘中）
            channel.basicPublish("", TASK_QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8));
            System.out.println("生产者发出消息：" + message);
        }
    }
}

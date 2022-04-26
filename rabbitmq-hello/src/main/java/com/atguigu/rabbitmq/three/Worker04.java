package com.atguigu.rabbitmq.three;

import com.atguigu.rabbitmq.utils.RabbitMqUtils;
import com.atguigu.rabbitmq.utils.SleepUtils;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;

/**
 * “生活本来很枯燥，但跑起来就有风”
 *
 * @author 杨正
 * @Package com.atguigu.rabbitmq.three
 * @date 2022/4/9 12:50
 * <p>
 * 消息在手动应答时，放回队列中重新消费
 */
public class Worker04 {
    public static final String TASK_QUEUE_NAME = "ack_queue";

    public static void main(String[] args) throws Exception {
        Channel channel = RabbitMqUtils.getChannel();
//        开启发布确认
        channel.confirmSelect();
        System.out.println("C2等待接收消息处理时间较长");

        //推送的消息如何进行消费的接口回调
        DeliverCallback deliverCallback=(consumerTag, message)->{
//           沉睡一秒
            SleepUtils.sleep(30);
            System.out.println("接收到的消息："+new String(message.getBody()));
//            手动应答
            /*
            1.消息的表示 tag
            2.是否批量应答 false:不批量应答信道中的消息 true:批量
            * */
            channel.basicAck(message.getEnvelope().getDeliveryTag(), false);
        };

        //取消消费的一个回调接口 如在消费的时候队列被删除掉了
        CancelCallback cancelCallback= consumerTag -> {
            System.out.println(consumerTag+"消息被取消消费接口回调逻辑");
        };

        //       设置不公平分发
//        int prefetchCount = 1;
//        channel.basicQos(prefetchCount);

        //      预取值是5
        int prefetchCount = 5;
        channel.basicQos(prefetchCount);

        boolean atuoAck = false;
        channel.basicConsume(TASK_QUEUE_NAME, atuoAck, deliverCallback, cancelCallback);
    }
}

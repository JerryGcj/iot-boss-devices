package com.wangxin.iot.config;

import com.rabbitmq.client.Channel;
import com.wangxin.iot.mq.BaseConsumer;
import lombok.extern.slf4j.Slf4j;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author: ****
 * @Date: 2020/3/21
 */
@Slf4j
@Component
public class ProxyBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if(bean.getClass().getName().startsWith("org.springframework")) {
            return bean;
        }
        if(BaseConsumer.class.isAssignableFrom(bean.getClass())){
            ProxyFactory proxyFactory = new ProxyFactory();
            proxyFactory.setTarget(bean);
            proxyFactory.addAdvice((MethodInterceptor) methodInvocation -> {
                Method method = methodInvocation.getMethod();
                Object[] args = methodInvocation.getArguments();
                Message message = (Message) args[0];
                Channel channel = (Channel) args[1];

                String correlationId = getCorrelationId(message);

                if (isConsumed(correlationId)) {// 消费幂等性, 防止消息被重复消费
                    log.info("重复消费, correlationId: {}", correlationId);
                    return null;
                }

                MessageProperties properties = message.getMessageProperties();
                long tag = properties.getDeliveryTag();

                try {
                    // 真正消费的业务逻辑
                    Object result = method.invoke(bean, args);
//                    msgLogService.updateStatus(correlationId, Constant.MsgLogStatus.CONSUMED_SUCCESS);
                    // 消费确认
                    channel.basicAck(tag, false);
                    return result;
                } catch (Exception e) {
                    log.error("消费者{}出现异常：{}", bean,e);
                        //业务准确性没那么高，这里即便consumer失败了，打印下日志即可，向mq确认成功
                    channel.basicAck(tag, false);
                    //TODO(此处应该重新投递到mq,但是异常后会死循环)
//                    channel.basicNack(tag, false, true);
                    return null;
                }
            });
            return proxyFactory.getProxy();
        }
        return bean;
    }
    /**
     * 获取CorrelationId
     *
     * @param message
     * @return
     */
    private String getCorrelationId(Message message) {
        String correlationId = null;

        MessageProperties properties = message.getMessageProperties();
        Map<String, Object> headers = properties.getHeaders();
        for (Map.Entry entry : headers.entrySet()) {
            String key = (String) entry.getKey();
            String value = (String) entry.getValue();
            if (key.equals("spring_returned_message_correlation")) {
                correlationId = value;
            }
        }

        return correlationId;
    }

    /**
     * 消息是否已被消费
     *
     * @param correlationId
     * @return
     */
    private boolean isConsumed(String correlationId) {
//        MsgLog msgLog = msgLogService.selectByMsgId(correlationId);
//        if (null == msgLog || msgLog.getStatus().equals(Constant.MsgLogStatus.CONSUMED_SUCCESS)) {
//            return true;
//        }

        return false;
    }
}

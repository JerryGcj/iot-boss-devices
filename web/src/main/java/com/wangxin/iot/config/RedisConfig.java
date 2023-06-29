package com.wangxin.iot.config;

import com.wangxin.iot.config.event.NeedSyncDetailsReceiver;
import com.wangxin.iot.config.event.OrderProfitReceiver;
import com.wangxin.iot.config.event.TelecomSyncDetailsReceiver;
import com.wangxin.iot.constants.RedisKeyConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @Description : RedisTemplate  服务封装类
 * @author: Mark (majianyou@wxdata.cn)
 * @version:V1.0
 * @Date: 2018/08/15
 */
@Configuration
@EnableAutoConfiguration
@Slf4j
public class RedisConfig {

    @Autowired
    TelecomSyncDetailsReceiver telecomSyncDetailsReceiver;
    @Autowired
    NeedSyncDetailsReceiver needSyncDetailsReceiver;

    @Bean
    @ConfigurationProperties(prefix="redis.pool")
    public JedisPoolConfig getRedisConfig(){
        JedisPoolConfig config = new JedisPoolConfig();
        return config;
    }

    @Bean
    @ConfigurationProperties(prefix="redis.config")
    public JedisConnectionFactory getConnectionFactory(){
        JedisPoolConfig config = getRedisConfig();
        JedisConnectionFactory factory = new JedisConnectionFactory(config);
        log.info("创建 JedisConnectionFactory bean init success. ");
        return factory;
    }


    @Bean
    @Primary  //在同样的RedisTemplate中，首先使用被标注的RedisTemplate
    public RedisTemplate getRedisTemplate(){
        RedisTemplate template = new StringRedisTemplate(getConnectionFactory());
        return template;
    }
    @Bean
    public MessageListenerAdapter telecomSyncAccessNumber(){
        return new MessageListenerAdapter(telecomSyncDetailsReceiver,"receiveMessage");
    }
    @Bean
    public RedisMessageListenerContainer container(MessageListenerAdapter telecomSyncAccessNumber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(getConnectionFactory());
        container.addMessageListener(telecomSyncAccessNumber, new PatternTopic(RedisKeyConstants.TELECOM_EQUIPMENT_CHANNEL.getMessage()));
        return container;
    }
    @Bean
    public MessageListenerAdapter unicomSyncAccessNumber(){
        return new MessageListenerAdapter(needSyncDetailsReceiver,"receiveMessage");
    }
    @Bean
    public RedisMessageListenerContainer unicomContainer(MessageListenerAdapter unicomSyncAccessNumber) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(getConnectionFactory());
        container.addMessageListener(unicomSyncAccessNumber, new PatternTopic(RedisKeyConstants.NEED_SYNC_CHANNEL.getMessage()));
        return container;
    }
}

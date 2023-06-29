package com.wangxin.iot.utils.redis;

import com.wangxin.iot.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * @Description : Redis Key  生成规则工具类
 * @author: Mark (majianyou@wxdata.cn)
 * @version: V1.0
 * @Date: 2018/08/15
 */
@Component
public class RedisKeyUtil {

	private static RedisUtil redisUtil;
	@Autowired
	public void setRedisUtil(RedisUtil redisUtil){
		RedisKeyUtil.redisUtil=redisUtil;
	}
	/**
	 * 按照规则生成RedisKey
	 * @param marketingId 活动ID
	 * @param scope 作用域
	 * @param args  其他参数
	 * @return redisKey
	 */
	public static Optional<String> createAndCheckKey(
					String marketingId, String scope,
					String ... args  ){
		Optional<String>  optKey =  Optional.ofNullable(null);
		if(StringUtil.isNotEmpty(marketingId) && StringUtil.isNotEmpty(scope)) {
			StringBuilder  sbKey = new StringBuilder();
			sbKey.append(marketingId);
			sbKey.append("_");
			sbKey.append(scope);
			for (String arg: args
			) {
				sbKey.append("_");
				sbKey.append(arg);
			}
			//判断Redis是否已经存在生成的KEY
			if(!redisUtil.hasKey(sbKey.toString())){
				optKey = Optional.of(sbKey.toString());
			}
		}
		return optKey;
	}

	/**
	 * 按照规则生成RedisKey
	 * @param marketingId 活动ID
	 * @param scope 作用域
	 * @param args  其他参数
	 * @return redisKey
	 */
	public static Optional<String> createKey(
					String marketingId, String scope,
					String ... args  ){
		if(StringUtil.isNotEmpty(marketingId) && StringUtil.isNotEmpty(scope)) {
			StringBuilder  sbKey = new StringBuilder();
			sbKey.append(marketingId);
			sbKey.append("_");
			sbKey.append(scope);
			for (String arg: args
			) {
				sbKey.append("_");
				sbKey.append(arg);
			}
			return Optional.of(sbKey.toString());
		}
		return Optional.empty();
	}

}

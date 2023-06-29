package com.wangxin.iot.delayed.entity;

import lombok.Data;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author anan
 * @date 2022/11/10 15:13
 */
@Data
public class IccidDelayed implements Delayed {
    private String iccid;
    private Long time;

    public IccidDelayed(String iccid, Long time,TimeUnit unit) {
        this.iccid = iccid;
        this.time = System.currentTimeMillis() + (time > 0? unit.toMillis(time): 0);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return time - System.currentTimeMillis();
    }

    @Override
    public int compareTo(Delayed o) {
        IccidDelayed item = (IccidDelayed) o;
        long diff = this.time - item.time;
        if (diff <= 0) {
            return -1;
        }else {
            return 1;
        }
    }
}

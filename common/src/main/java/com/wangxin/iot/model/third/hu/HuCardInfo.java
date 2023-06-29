package com.wangxin.iot.model.third.hu;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yanwin
 * @Date: 2020/3/3
 */
@NoArgsConstructor
@Data
public class HuCardInfo {

    /**
     * card : {"id":22390,"iccid":"89860419151871818523","msisdn":"1440193898523","status":3,"active_time":null,"on_off":0,"ip":"10.133.150.185","data":"0.0000","total_flow":"0.0000","refresh_time":null,"term_end_time":1567267199}
     * package : null
     * error :
     */

    private CardInfo card;
    private String packageX;
    private String error;
    private String message;

}

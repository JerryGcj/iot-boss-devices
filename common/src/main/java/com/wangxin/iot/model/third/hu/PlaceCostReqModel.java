package com.wangxin.iot.model.third.hu;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author: yanwin
 * @Date: 2020/3/3
 */
@NoArgsConstructor
@Data
public class PlaceCostReqModel {
    /**
     * card : {"iccid":"123"}
     * term_length : 1
     * price : 12.34
     * money : 24.68
     * trade_type : 1
     * trade_no : 123
     * trans_id : 123-234-345
     * package_id : 1
     * source : 1
     * input : ***
     * output : ***
     * notify : ***
     * extra_info : {"username":"测试","tel":"123","message":"测试充值"}
     */

    private CardInfo card;
    private int term_length;
    private String price;
    private String money;
    private int trade_type;
    private String trade_no;
    private String trans_id;
    private String package_id;
    private String source;
    private String input;
    private String output;
    private String notify;
    private ExtraInfo extra_info;
    @Data
    class ExtraInfo{
        private String username;
        private String tel;
        private String message;
    }
}

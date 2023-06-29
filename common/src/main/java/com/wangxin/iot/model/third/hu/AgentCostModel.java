package com.wangxin.iot.model.third.hu;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author: yanwin
 * @Date: 2020/3/3
 */
@NoArgsConstructor
@Data
public class AgentCostModel {

    /**
     * code : 1
     * msg : 操作成功
     * datas : [{"id":"1","name":"飞享大唐卡10G基础包","data":"10737418240.0000","cost_price":"8.0000","isp_id":"1","isp_name":"联通大唐卡","type":"1"}]
     */

    private String code;
    private String msg;
    private List<CostModel> datas;
    @Data
    class CostModel{
        private String id;
        private String name;
        private String data;
        private String costPrice;
        private String ispId;
        private String ispName;
        private String type;
    }
}

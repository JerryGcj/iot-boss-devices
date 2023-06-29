package com.wangxin.iot.rest.base;

import java.util.List;

/**
 * @ClassName : PageResult
 * @Description : 分页数据结果封装类
 * @author: Mark (majianyou@wxdata.cn)
 * @version: V1.0
 * @Date: 2018-8-13
 */
public class PageResult<T> {

    private List<T> data;

    private Integer total;

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }
}

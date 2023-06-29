package com.wangxin.iot.typehandler;

import org.apache.ibatis.type.LocalDateTypeHandler;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * @author: yanwin
 * @Date: 2020/5/26
 */
@Component
public class MyLocalDateTypeHandler extends LocalDateTypeHandler {
    @Override
    public LocalDate getResult(ResultSet rs, String columnName) throws SQLException {
        Object object = rs.getObject(columnName);
        if(object instanceof java.sql.Date){
            //在这里强行转换，将sql的时间转换为LocalDate
            Date date = (Date) object;
            return date.toLocalDate();
        }
        return super.getResult(rs, columnName);
    }
}

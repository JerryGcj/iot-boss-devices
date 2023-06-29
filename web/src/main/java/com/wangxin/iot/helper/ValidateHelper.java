package com.wangxin.iot.helper;


import lombok.extern.slf4j.Slf4j;
import java.util.*;
import javax.validation.*;

/**
 * @version: V1.0
 * @description: 入参判断
 * @author: 济南研发-张闻帅
 * @create: 2019-08-21 10:55
 **/
@Slf4j
public class ValidateHelper {

    /**
     *  获得验证的工厂类
     */
    private static ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    /**
     * 验证数据
     * @param t 需要验证的数据
     * @param <T> 验证数据的类型
     * @return 验证不通过的提示信息
     */
    public static <T> List<String> validate(T t) {
        Validator validator = factory.getValidator();
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(t);
        List<String> messageList = new ArrayList<>();

        for (ConstraintViolation<T> constraintViolation : constraintViolations) {
            StringBuilder sbString=new StringBuilder();

            //获得 校验信息的依赖关系，生成字符串
            Path path=constraintViolation.getPropertyPath();
            //生成Set去重复集合，存放获得每一级的索引值
            Set<Integer> setIndex=new HashSet<>();
            path.forEach(node -> {
                if(node.getIndex()!=null){
                    setIndex.add(node.getIndex());
                }
            });
            //遍历SET 获得索引值，字符串替换 索引值+1，获得标准编号
            setIndex.forEach(index->{
                sbString.delete( 0, sbString.length() );
                sbString.append(path.toString().replace("["+index+"]"," 第 "+(index.intValue()+1)+" 行 "));
            });
            //获得校验信息
            StringBuilder sbMess = new StringBuilder();
            if(sbString.length()>0){
                sbMess.append(sbString.toString()+" ：");
            }
            sbMess.append(constraintViolation.getMessage());
            messageList.add(sbMess.toString());
        }
        return messageList;
    }
}

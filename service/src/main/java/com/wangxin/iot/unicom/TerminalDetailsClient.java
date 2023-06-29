/**
 * Copyright 2005 Jasper Systems, Inc. All rights reserved.
 *
 * This software code is the confidential and proprietary information of
 * Jasper Systems, Inc. ("Confidential Information"). Any unauthorized
 * review, use, copy, disclosure or distribution of such Confidential
 * Information is strictly prohibited.
 */
package com.wangxin.iot.unicom;

import com.wangxin.iot.config.SoapConfigProerties;
import com.wangxin.iot.constants.CardStatusConstants;
import com.wangxin.iot.mobile.AbstractIotApiClient;
import com.wangxin.iot.model.Card;
import com.wangxin.iot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.soap.Name;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class TerminalDetailsClient extends AbstractIotApiClient {

    @Autowired
    private SoapConfigProerties soapConfig;


    public TerminalDetailsClient(){
        //注意，此处必须符合联通文档要求
        super("GetTerminalDetails");
    }
    @Override
    protected SOAPEnvelope createBody(SOAPEnvelope envelope,SOAPBodyElement terminalRequestElement, List<String> queryIccids){
       try {
           Name iccids = envelope.createName("iccids", soapConfig.getPrefix(), soapConfig.getNamespaceUrl());
           SOAPElement iccidsElement = terminalRequestElement.addChildElement(iccids);
           for (String iccid:queryIccids) {
               Name iccidName = envelope.createName("iccid",soapConfig.getPrefix(), soapConfig.getNamespaceUrl());
               SOAPElement iccidElement = iccidsElement.addChildElement(iccidName);
               iccidElement.setValue(iccid);
           }
       }catch (Exception e){
           e.printStackTrace();
       }
       return envelope;
    }

    @Override
    protected void handlerTerminal(SOAPBodyElement terminalsElements, Name terminal) {
        Iterator itr = terminalsElements.getChildElements(terminal);
        while ( itr.hasNext()) {
            SOAPBodyElement element = (SOAPBodyElement) itr.next();
            NodeList list = element.getChildNodes();
            Node n ;
//            CardMessage card = new CardMessage();
            Card card = new Card();
            String msisdn = element.getAttribute("msisdn");
            String iccid ="";
            for (int i = 0; i < list.getLength(); i ++) {
                n = list.item(i);
                card.setMsisdn(msisdn);
                if ( n.getLocalName() != null && !"null".equals(n.getLocalName())){
                   try {
                       //iccid
                       if("iccid".equals(n.getLocalName())){
                           iccid = n.getTextContent();
                           card.setIccid(iccid);
                       }
                       //卡状态
                       if("status".equals(n.getLocalName())){
                           card.setStatus(CardStatusConstants.getCode(n.getTextContent()));
                       }
                       //
                       if("imsi".equals(n.getLocalName())){
                           card.setImsi(n.getTextContent());
                       }
                       if("imei".equals(n.getLocalName())){
                           card.setImei(n.getTextContent());
                       }
                       //开启状态
                       if("suspended".equals(n.getLocalName())){
                           card.setOnOff(n.getTextContent().equals("Y")?1:0);
                       }
                       //本月流量值
                       if("monthToDateUsage".equals(n.getLocalName())){
                           BigDecimal monthUsage = new BigDecimal(n.getTextContent());
                           card.setData(monthUsage);
                       }
                       //短信
                       if("monthToDateSMSUsage".equals(n.getLocalName())){
                           card.setSms(Integer.valueOf(n.getTextContent()));
                       }
                       //语音
                       if("monthToDateVoiceUsage".equals(n.getLocalName())){
                           card.setVoice(Integer.valueOf(n.getTextContent()));
                       }
                       //激活时间
                       if("dateActivated".equals(n.getLocalName())){
                           card.setActiveTime(DateUtils.formatStringToDate(n.getTextContent()));
                       }
                       if("accountId".equals(n.getLocalName())){
                           card.setAccountId(n.getTextContent());
                       }
                       if("operatorCustom1".equals(n.getLocalName())){
                           card.setOperatorCustom(n.getTextContent());
                       }
                       if("ctdSessionCount".equals(n.getLocalName())){
                           card.setCtdSessionCount(n.getTextContent());
                       }
                       if("ratePlan".equals(n.getLocalName())){
                           card.setRatePlan(n.getTextContent());
                       }
                       if("customer".equals(n.getLocalName())){
                           card.setCustomer(n.getTextContent());
                       }
                       if("custom10".equals(n.getLocalName())){
                           card.setCustom10(n.getTextContent());
                       }
                       if("dateAdded".equals(n.getLocalName())){
                           card.setDateAdded(n.getTextContent());
                       }
                       if("dateModified".equals(n.getLocalName())){
                           card.setDateModified(n.getTextContent());
                       }
                       if("dateShipped".equals(n.getLocalName())){
                           card.setDateShipped(n.getTextContent());
                       }
                   }catch (Exception e){
                       e.printStackTrace();
                   }
                }
            }
            //TODO(mq)
//            Boolean delivery = this.publisher.publisher(MqConstants.CARD_QUEUE_NAME.getMessage(), MqConstants.CARD_EXCHANGE_NORMAL_ROUTING_KEY.getMessage(), card);
           //没有投递成功，则往Java的blockqueue中投递
//            try {
//                Card blockCard = new Card();
//                BeanUtils.copyProperties(card, blockCard);
//                //入队
//                HandlerThread.CARD_QUEUE.put(blockCard);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            //入队
            try {
                HandlerThread.CARD_QUEUE.put(card);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}


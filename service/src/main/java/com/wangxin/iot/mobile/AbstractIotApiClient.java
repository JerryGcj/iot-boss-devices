package com.wangxin.iot.mobile;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;
import com.wangxin.iot.config.SoapConfigProerties;
import com.wangxin.iot.config.TemplateConfig;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.soap.*;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by 18765 on 2020/1/2 10:28
 */
@Slf4j
public abstract class AbstractIotApiClient implements IotApiClient {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private SoapConfigProerties soapConfig;
    private SOAPConnectionFactory connectionFactory;
    private MessageFactory messageFactory;
    private URL url;
    private XWSSProcessorFactory processorFactory;
    private String soapApi;
    @PostConstruct
    public  void demo() throws MalformedURLException {
        this.url = new URL(soapConfig.getUrl());
    }

    public AbstractIotApiClient(String soapApi){
        try {
            connectionFactory = SOAPConnectionFactory.newInstance();
            messageFactory = MessageFactory.newInstance();
            processorFactory = XWSSProcessorFactory.newInstance();
            this.soapApi = soapApi;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * TODO(改为异步)
     *    @Async
     * @param iccids
     */
    @Override
    public  void callWebService(List iccids, TemplateConfig config) {
        if(CollectionUtils.isEmpty(iccids)){
            return;
        }
        try {
            SOAPMessage soapMessage = this.createTerminalRequest(iccids,config);
            soapMessage = this.secureMessage(soapMessage,config);
            System.out.println("Request: ");
            soapMessage.writeTo(System.out);
            SOAPConnection connection = connectionFactory.createConnection();
            SOAPMessage response = connection.call(soapMessage, url);
            System.out.println("Response: ");
            response.writeTo(System.out);
            if (!response.getSOAPBody().hasFault()) {
                writeTerminalResponse(response);
            } else {
                SOAPFault fault = response.getSOAPBody().getFault();
                log.error("Received SOAP Fault:\t Fault Code:{},Fault String{}",fault.getFaultCode(),fault.getFaultString());
                System.err.println("SOAP Fault Code :" + fault.getFaultCode()+"\n"+"SOAP Fault String :" + fault.getFaultString());
                //调用失败后，放入缓存，后台线程再次查询该批单子。
                //TODO(失败重提，3次)
                redisUtil.lSetList(RedisKeyConstants.TASK_ACTIVE_ERROR.getMessage(),iccids);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    /**
     * 自己创建请求体
     * @param envelope
     * @param soapBodyElement
     * @param iccids
     * @return
     */
    protected  abstract SOAPEnvelope createBody(SOAPEnvelope envelope,SOAPBodyElement soapBodyElement, List<String> iccids);
    /**
     * 自定义处理结果
     * @param terminalsElements
     * @param terminal
     */
    protected abstract void handlerTerminal(SOAPBodyElement terminalsElements,Name terminal);

    /**
     * 创建请求
     * @param iccids
     * @return
     */
    private SOAPMessage createTerminalRequest(List<String> iccids, TemplateConfig config){
        SOAPMessage message = null;
        try {
            message = messageFactory.createMessage();
            message.getMimeHeaders().addHeader("SOAPAction",
                    "http://api.jasperwireless.com/ws/service/terminal/"+soapApi);
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            Name terminalRequestName = envelope.createName(soapApi+"Request", soapConfig.getPrefix(), soapConfig.getNamespaceUrl());
            SOAPBodyElement terminalRequestElement = message.getSOAPBody()
                    .addBodyElement(terminalRequestName);
            Name msgId = envelope.createName("messageId", soapConfig.getPrefix(), soapConfig.getNamespaceUrl());
            SOAPElement msgElement = terminalRequestElement.addChildElement(msgId);
            msgElement.setValue("TCE-100-ABC-34084");
            Name version = envelope.createName("version", soapConfig.getPrefix(), soapConfig.getNamespaceUrl());
            SOAPElement versionElement = terminalRequestElement.addChildElement(version);
            versionElement.setValue("1.0");
            Name license = envelope.createName("licenseKey", soapConfig.getPrefix(), soapConfig.getNamespaceUrl());
            SOAPElement licenseElement = terminalRequestElement.addChildElement(license);
            licenseElement.setValue(config.getLicenseKey());
            this.createBody(envelope,terminalRequestElement,iccids);
        } catch (Exception e) {
            log.error("create SOAPMessage error!，reason:{}",e.getMessage());
            e.printStackTrace();
        }
        return message;
    }

    /**
     * 处理返回
     * @param message
     */
    private void writeTerminalResponse(SOAPMessage message) {
        try {
            SOAPEnvelope envelope = message.getSOAPPart().getEnvelope();
            Name terminalResponseName = envelope.createName(this.soapApi+"Response", soapConfig.getPrefix(), soapConfig.getNamespaceUrl());
            SOAPBodyElement terminalResponseElement = (SOAPBodyElement) message
                    .getSOAPBody().getChildElements(terminalResponseName).next();
            Name terminals = envelope.createName("terminals", soapConfig.getPrefix(), soapConfig.getNamespaceUrl());
            Name terminal = envelope.createName("terminal", soapConfig.getPrefix(), soapConfig.getNamespaceUrl());
            SOAPBodyElement terminalsElements = (SOAPBodyElement) terminalResponseElement.getChildElements(terminals).next();
            log.info("soap\\t"+soapApi+"response：{}",terminalsElements.toString());
            this.handlerTerminal(terminalsElements,terminal);
        }catch (Exception e){
            log.error("soap\t"+soapApi+"\terror reason：{}",e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 发送请求
     * @param message
     * @return
     */
    private SOAPMessage secureMessage(SOAPMessage message, TemplateConfig config) {
        try {
            CallbackHandler callbackHandler = callbacks -> {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof UsernameCallback) {
                        UsernameCallback callback = (UsernameCallback) callbacks[i];
                        callback.setUsername(config.getUsername());
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        PasswordCallback callback = (PasswordCallback) callbacks[i];
                        callback.setPassword(config.getPassword());
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i]);
                    }
                }
            };
            InputStream policyStream = null;
            XWSSProcessor processor = null;
            try {
                policyStream = getClass().getResourceAsStream("/securityPolicy.xml");
                processor = processorFactory.createProcessorForSecurityConfiguration(policyStream, callbackHandler);
            }catch (Exception e){
                e.printStackTrace();
            }
            finally {
                if (policyStream != null) {
                    policyStream.close();
                }
            }
            ProcessingContext context = processor.createProcessingContext(message);
            return processor.secureOutboundMessage(context);
        }catch (Exception e){

        }
        return null;
    }


}

package com.wangxin.iot.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * HttpClient工具类， 封装了一些采用HttpClient发送HTTP请求的方法
 * 
 * @author WLing
 * 
 */
public class HttpClientHelper {
	private final static Logger logger = LoggerFactory.getLogger(HttpClientHelper.class);
	//默认采用的http协议的HttpClient对象
	private static CloseableHttpClient httpClient;
	//默认采用的https协议的HttpClient对象
	private static CloseableHttpClient httpsClient;
	private static RequestConfig requestConfig;
	private static PoolingHttpClientConnectionManager connManager;
	private static final int MAX_TIMEOUT1 = 20000; // 7000ms
	private static final int MAX_TIMEOUT2 = 80000; //连接超时 
	private static final int MAX_TIMEOUT3 = 100000; //请求超时
	public static String CHARSET_DEFAULT = "UTF-8";
	
	//httpURLConnection.setRequestProperty("connection", "Keep-Alive");
    //设置cookie管理策略
    //client.getState().setCookiePolicy(CookiePolicy.COMPATIBILITY);
	/**
	 * 最大允许连接数
	 */
	private static final int MAX_TOTAL_CONNECTION = 800;
	static {
		//采用绕过验证的方式处理https请求  
	    SSLContext sslcontext = createIgnoreVerifySSL();  
       // 设置协议http和https对应的处理socket链接工厂的对象  
       Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()  
           .register("http", PlainConnectionSocketFactory.INSTANCE)  
           .register("https", new SSLConnectionSocketFactory(sslcontext))  
           .build();  
		// 设置连接池
		connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
		
		// 设置连接池大小
		connManager.setMaxTotal(MAX_TOTAL_CONNECTION);
		connManager.setDefaultMaxPerRoute(300);//WL：2016-12-29出现异常ConnectionPoolTimeoutException将40改为300
		// 设置连接超时	// 设置全局的标准cookie策略r.setCookieSpec(CookieSpecs.STANDARD_STRICT);
		/*设置ConnectionPoolTimeout：从连接池中取连接的超时时间;ConnectionPoolTimeoutException
		 *设置ConnectionTimeout：连接超时 ,这定义了通过网络与服务器建立连接的超时时间。ConnectionTimeoutException  ==>HttpHostConnectException
		 *设置 SocketTimeout：请求超时即读取超时 ,这定义了Socket读数据的超时时间，即从服务器获取响应数据需要等待的时间，此处设置为4秒。SocketTimeoutException*/
		requestConfig = RequestConfig.custom().setConnectionRequestTimeout(MAX_TIMEOUT1).setConnectTimeout(MAX_TIMEOUT2).setSocketTimeout(MAX_TIMEOUT3).build();
		//httpClient = HttpClients.custom().setConnectionManager(connManager).setDefaultRequestConfig(requestConfig).build();// 设置可关闭的HttpClient
		httpClient = HttpClients.custom().setConnectionManager(connManager).setConnectionManagerShared(true).setDefaultRequestConfig(requestConfig).build();
		
		httpsClient = HttpClients.createDefault();
	}
	/** 
	 * 绕过验证 
	 *   
	 * @return 
	 */  
	public static SSLContext createIgnoreVerifySSL() {  
	    SSLContext sc = null;
		try {
			sc = SSLContext.getInstance("SSL");
			// 实现一个X509TrustManager接口，用于绕过验证，不用修改里面的方法  
		    X509TrustManager trustManager = new X509TrustManager() {  
		        @Override  
		        public void checkClientTrusted(  
		                X509Certificate[] paramArrayOfX509Certificate,
		                String paramString) throws CertificateException {  
		        }  
		  
		        @Override  
		        public void checkServerTrusted(  
		                X509Certificate[] paramArrayOfX509Certificate,
		                String paramString) throws CertificateException {  
		        }  
		  
		        @Override  
		        public X509Certificate[] getAcceptedIssuers() {
		            return null;  
		        }  
		    };  
		    sc.init(null, new TrustManager[] { trustManager }, new java.security.SecureRandom());
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}catch (KeyManagementException e) {
			e.printStackTrace();
		}  
	    return sc;  
	} 
	/**
	 * 释放httpClient连接
	 */

	public static void shutdown() {
		//connManager.shutdown();
		try {
			httpClient.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 组装头部
	 * 
	 * @param map
	 *            头部map信息
	 * @return
	 */
	public static Header[] builderHeader(Map<String, String> map) {
		Header[] headers = new BasicHeader[map.size()];
		int i = 0;
		for (String str : map.keySet()) {
			headers[i] = new BasicHeader(str, map.get(str));
			i++;
		}
		return headers;
	}

	/**
	 * 发送 GET 请求（HTTP），对应的参数连接，例如：account=qdwangxkj&mobile=13105186219&package=0
	 * 
	 * @param params
	 * @return
	 */
	public static String getParams(Map<String, Object> params,boolean urlFlag) {
		StringBuffer param = new StringBuffer();
		int i = 0;
		for (String key : params.keySet()) {
			if (i == 0 && urlFlag)
				param.append("?");
			else if(i!=0){
				param.append("&");
			}
			param.append(key).append("=").append(params.get(key));
			i++;
		}
		return param.toString();
		
	}
	public static String getParamsByDesc(Map<String, Object> params) {//降序
		List<Entry<String,Object>> entryList = new ArrayList<Entry<String,Object>>(params.entrySet());
		Collections.sort(entryList, new Comparator<Entry<String,Object>>() {

			@Override
			public int compare(Entry<String, Object> o1,
					Entry<String, Object> o2) {
				return	(o1.getKey().compareTo(o2.getKey()));
			}
		});
		StringBuffer param = new StringBuffer();
		for(Entry<String,Object> entry: entryList){
			param.append(entry.getKey()).append("=").append(entry.getValue());
			param.append("&");
		}
		return param.toString();
		
	}
	public static String getParamsByAscending(Map<String, String> params) {// 升序
		List<Entry<String, String>> entryList = new ArrayList<Entry<String, String>>(
				params.entrySet());
		Collections.sort(entryList,
				new Comparator<Entry<String, String>>() {

					@Override
					public int compare(Entry<String, String> o1,
							Entry<String, String> o2) {
						//System.out.println(o2.getKey().compareTo(o1.getKey()));
						return (o1.getKey().compareTo(o2.getKey()));
					}
				});
		StringBuffer param = new StringBuffer();
		for (Entry<String, String> entry : entryList) {
			param.append(entry.getKey()).append("=").append(entry.getValue());
			param.append("&");
		}
		return param.toString();

	}
	public static String toFrom(Map<String, String> params) {//map转k-V
		List<Entry<String, String>> entryList = new ArrayList<Entry<String, String>>(
				params.entrySet());
		StringBuffer param = new StringBuffer();
		for (Entry<String, String> entry : entryList) {
			param.append(entry.getKey()).append("=").append(entry.getValue());
			param.append("&");
		}
		return param.toString();

	}
	/**
	 * 发送 GET 请求（HTTP），K-V形式
	 * 
	 * @param url
	 * @param params
	 * @return
	 * @throws IOException 
	 */
	public static String get(String url, Map<String, Object> params , String encode) {
		url += getParams(params,true);
		logger.info("==>GET请求URL："+url);
		String result = null;
		// HttpClient httpClient = new DefaultHttpClient();
		CloseableHttpResponse response = null;
		HttpGet httpGet = null;
		try {
			//logger.info("请求参数"+url);
			httpGet = new HttpGet(url);
			try {
				if(url.toLowerCase().startsWith("https://")){
					response = httpClient.execute(httpGet);//TODO:===============????????????
				}else{
					response = httpClient.execute(httpGet);
				}
				logger.info("响应："+response);
			}catch (Exception e) {//其他异常
				logger.warn("==>Exception异常:"+e.toString());
				e.printStackTrace();
			}
			HttpEntity entity = response.getEntity();
			try {
				result = EntityUtils.toString(entity,encode);
				//logger.info("==>响应报文:"+result);
			} catch (Exception e) {//响应异常
				logger.error("==>解析响应报文异常:"+e.toString());
				e.printStackTrace();
			}
		}finally {
			if (response != null) {
				try {
					//释放资源
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					logger.info("",e);
					e.printStackTrace();
				}finally {
					//关闭CloseableHttpResponse
					try {
						response.close();
					} catch (IOException e) {
						logger.info("",e);
						e.printStackTrace();
					}
				}
			}
			httpGet.releaseConnection();
			shutdown();
		}
		return result;
	}
	/**
	 * 发送 GET 请求（HTTP），K-V形式
	 *
	 * @param url
	 * @param
	 * @return
	 * @throws IOException
	 */
	public static String getForTencent(String url, String encode) {
		String result = null;
		// HttpClient httpClient = new DefaultHttpClient();
		CloseableHttpResponse response = null;
		HttpGet httpGet = null;
		try {
			logger.info("请求参数"+url);
			httpGet = new HttpGet(url);
			try {
				if(url.toLowerCase().startsWith("https://")){
					response = httpsClient.execute(httpGet);//TODO:===============????????????
				}else{
					response = httpClient.execute(httpGet);
				}
			} catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
			HttpEntity entity = response.getEntity();
			try {
				result = EntityUtils.toString(entity,encode);
				//logger.info("==>响应结果"+result);
			} catch (Exception e) {//响应异常
				logger.error("==>解析响应结果异常"+e.toString());
				e.printStackTrace();
			}
		}finally {
			if (response != null) {
				try {
					//释放资源
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					logger.info("",e);
					e.printStackTrace();
				}finally {
					//关闭CloseableHttpResponse
					try {
						response.close();
					} catch (IOException e) {
						logger.info("",e);
						e.printStackTrace();
					}
				}
			}
			httpGet.releaseConnection();
			shutdown();
		}
		return result;
	}
	/**
	 * 发送 GET 请求（HTTP），K-V形式
	 * 
	 * @param url
	 * @param
	 * @return
	 * @throws IOException 
	 */
	public static String get(String url, String encode) {
		String result = null;
		// HttpClient httpClient = new DefaultHttpClient();
		CloseableHttpResponse response = null;
		HttpGet httpGet = null;
		try {
			logger.info("请求参数"+url);
			httpGet = new HttpGet(url);
			try {
				if(url.toLowerCase().startsWith("https://")){
					response = httpsClient.execute(httpGet);//TODO:===============????????????
				}else{
					response = httpClient.execute(httpGet);
				}
			} catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
			HttpEntity entity = response.getEntity();
			try {
				result = EntityUtils.toString(entity,encode);
				//logger.info("==>响应结果"+result);
			} catch (Exception e) {//响应异常
				logger.error("==>解析响应结果异常"+e.toString());
				e.printStackTrace();
			}
		}finally {
			if (response != null) {
				try {
					//释放资源
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					logger.info("",e);
					e.printStackTrace();
				}finally {
					//关闭CloseableHttpResponse
					try {
						response.close();
					} catch (IOException e) {
						logger.info("",e);
						e.printStackTrace();
					}
				}
			}
			httpGet.releaseConnection();
			shutdown();
		}
		return result;
	}
	
	/*
	 * 只是暂时适用于调用百度的一个号码查询接口
	 */
	public static String get(String httpUrl, Map<String, Object> params  , String encode , String apiKey) {
	    BufferedReader reader = null;
	    String result = null;
	    StringBuffer sbf = new StringBuffer();
	    httpUrl += getParams(params,true);

	    try {
	        URL url = new URL(httpUrl);
	        HttpURLConnection connection = (HttpURLConnection) url
	                .openConnection();
	        connection.setRequestMethod("GET");
	        // 填入apikey到HTTP header
	        connection.setRequestProperty("apikey",  apiKey);
	        connection.connect();
	        InputStream is = connection.getInputStream();
	        reader = new BufferedReader(new InputStreamReader(is, encode));
	        String strRead = null;
	        while ((strRead = reader.readLine()) != null) {
	            sbf.append(strRead);
	            sbf.append("\r\n");
	        }
	        reader.close();
	        result = sbf.toString();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    return result;
	}
	
	
	 //山东联通的请求
	public static String post(String url, String params) {
		url += params;
		String result = null;
		CloseableHttpResponse response = null;
		HttpPost httpPost = null;
		try {
			logger.info("请求参数"+url);
			httpPost = new HttpPost(url);
			try {
				if(url.toLowerCase().startsWith("https://")){
					response = httpsClient.execute(httpPost);
				}else{
					response = httpClient.execute(httpPost);
				}
			} catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
			HttpEntity entity = response.getEntity();
			try {
				result = EntityUtils.toString(entity,"UTF-8");
				//logger.info("==>响应结果"+result);
			}catch (Exception e) {//其他异常
				logger.error("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
		}finally {
			if (response != null) {
				try {
					//释放资源
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					logger.info("",e);
					e.printStackTrace();
				}finally {
					//关闭CloseableHttpResponse
					try {
						response.close();
					} catch (IOException e) {
						logger.info("",e);
						e.printStackTrace();
					}
				}
			}
			httpPost.releaseConnection();
			shutdown();
		}
		return result;
	}

	/**
	 * 发送 POST 请求（HTTP），【查询参数使用json格式】，并返回字符串
	 * 
	 * @param url
	 *            请求地址
	 * @param json
	 *            请求json参数
	 * @return
	 * @throws IOException 
	 * @throws ParseException 
	 */
	public static String post(String url, String json, String charSet) {
		Header[] headers = new Header[] {new BasicHeader("Content-Type", "application/json")};
		StringEntity stringEntity = new StringEntity(json, charSet);// 解决中文乱码问题
		stringEntity.setContentEncoding(charSet);
		String httpStr = null;
		CloseableHttpResponse response = null;
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(stringEntity);
		httpPost.setHeaders(headers);
		try {
			try {
				response = httpClient.execute(httpPost);
			} catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
			HttpEntity entity = response.getEntity();
			try {
				httpStr = EntityUtils.toString(entity, charSet);
				//logger.info("响应结果"+httpStr);
			}catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
		}finally {
			if (response != null) {
				try {
					//释放资源
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}finally {
					//关闭CloseableHttpResponse
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			httpPost.releaseConnection();
			//关闭HttpClient
			shutdown();
		}
		return httpStr;
	}
	/**
	 * 发送 POST 请求（HTTP），K-V形式
	 * TODO   可能有map传过去以后拼接成a=1&b=1类型的字符串
	 * @param url API接口URL
	 * @param params 参数map
	 * @return
	 * @throws 
	 */
	public static String post(String url, Map<String, Object> params,String charSet) {
		String httpStr = null;
		HttpPost httpPost = new HttpPost(url);
		CloseableHttpResponse response = null;
		try {
			List<NameValuePair> pairList = new ArrayList<NameValuePair>(params.size());
			for (Entry<String, Object> entry : params.entrySet()) {
				NameValuePair pair = new BasicNameValuePair(entry.getKey(),entry.getValue().toString());
				pairList.add(pair);
			}
			UrlEncodedFormEntity formentity = new UrlEncodedFormEntity(pairList, Charset.forName(charSet));
			httpPost.setEntity(formentity);
			try {
				if(url.toLowerCase().startsWith("https://")){
					response = httpsClient.execute(httpPost);
				}else{
					response = httpClient.execute(httpPost);
				}
			} catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
			HttpEntity entity = response.getEntity();
			try {
				httpStr = EntityUtils.toString(entity, charSet);
				logger.info("响应结果"+httpStr);
			} catch (Exception e) {
				logger.error("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
		}finally {
			if (response != null) {
				try{
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}finally {
					//关闭CloseableHttpResponse
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			httpPost.releaseConnection();
			shutdown();
		}
		return httpStr;
	}

	
	/**
	 * 发送get请求【查询参数使用字符串】，并返回字符串
	 *
	 * @return
	 */
	public static String getHeaders(String url,Map<String, String> headers, String charSet) {
		String httpStr = null;
		CloseableHttpResponse response = null;
		HttpGet httpGet = new HttpGet(url);
		httpGet.setHeaders(builderHeader(headers));
		try {
			try {
				if(url.toLowerCase().startsWith("https://")){
					response = httpsClient.execute(httpGet);
				}else{
					response = httpClient.execute(httpGet);
				}
				logger.info("响应："+response);
			}catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
			HttpEntity entity = response.getEntity();
			try {
				httpStr = EntityUtils.toString(entity, charSet);
				logger.info("响应结果"+httpStr);
			}catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
		}finally {
			if (response != null) {
				try {
					//释放资源
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}finally {
					//关闭CloseableHttpResponse
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			httpGet.releaseConnection();
			//关闭HttpClient
			shutdown();
		}
		return httpStr;
	}
	
	/**
	 * 发送post请求【查询参数使用字符串】，并返回字符串
	 *
	 * @return
	 */
	public static String post(String url, String requestContext,Map<String, String> headers, String charSet) {
		StringEntity stringEntity = new StringEntity(requestContext, charSet);// 解决中文乱码问题
		stringEntity.setContentEncoding(charSet);
		String httpStr = null;
		CloseableHttpResponse response = null;
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(stringEntity);
		httpPost.setHeaders(builderHeader(headers));
		try {
			try {
				if(url.toLowerCase().startsWith("https://")){
					response = httpsClient.execute(httpPost);
				}else{
					response = httpClient.execute(httpPost);
				}
				try {
					logger.info("响应："+response+"Ma:"+response.getStatusLine().getStatusCode());
				} catch (Exception e) {
				}
			}catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
			HttpEntity entity = response.getEntity();
			try {
				httpStr = EntityUtils.toString(entity, charSet);
				logger.info("响应结果"+httpStr);
			}catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
		}finally {
			if (response != null) {
				try {
					//释放资源
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}finally {
					//关闭CloseableHttpResponse
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			httpPost.releaseConnection();
			//关闭HttpClient
			shutdown();
		}
		return httpStr;
	}
	
	/**
	 * 发送post请求【查询参数使用独立的参数生成】并返回字符串
	 * 
	 * @param url 请求地址
	 * @param map 请求参数Map
	 * @param headers 请求头部Map
	 * @param charSet 请参数编码格式
	 * @return 
	 */
	public static String post(String url, Map<String, String> map,Map<String, String> headers, String charSet) {
		String httpStr = null;
		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
		for (String key : map.keySet()) {
			NameValuePair nvp = new BasicNameValuePair(key, map.get(key));
			nvps.add(nvp);
		}
		try {
			HttpEntity entity = new UrlEncodedFormEntity(nvps, charSet);
			HttpPost httpPost = null;
			CloseableHttpResponse response = null;
			try {
				httpPost = new HttpPost(url);
				httpPost.setEntity(entity);
				httpPost.setHeaders(builderHeader(headers));
				try {
					response = httpClient.execute(httpPost);
					logger.info("响应："+response);
				}catch (Exception e) {//其他异常//
					logger.warn("==>Exception异常"+e.toString());
					e.printStackTrace();
				}
				HttpEntity httpEntity = response.getEntity();
				try {
					httpStr = EntityUtils.toString(httpEntity, charSet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} finally {
				if (response != null) {
					try {
						EntityUtils.consume(response.getEntity());
					} catch (IOException e) {
						e.printStackTrace();
					}finally {
						//关闭CloseableHttpResponse
						try {
							response.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				httpPost.releaseConnection();
				shutdown();
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return httpStr;
	}
	
	/**
	 * HJ 2016年10月26日12:00:23修改--返回的是map
	 * 山东移动用
	 * 发送post请求【查询参数使用字符串】，并返回字符串
	 *
	 * @return
	 */
	public static Map<String, String> postReturnMap(String url, String requestContext,Map<String, String> headers, String charSet) {
		StringEntity stringEntity = new StringEntity(requestContext, charSet);// 解决中文乱码问题
		stringEntity.setContentEncoding(charSet);
		Map<String, String> returnMap = new HashMap<>();
		String httpStr = null;
		String httpCodeStr = null;
		CloseableHttpResponse response = null;
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(stringEntity);
		httpPost.setHeaders(builderHeader(headers));
		try {
			try {
				if(url.toLowerCase().startsWith("https://")){
					response = httpsClient.execute(httpPost);
				}else{
					response = httpClient.execute(httpPost);
				}
			}catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
			HttpEntity entity = response.getEntity();
			try {
				httpStr = EntityUtils.toString(entity, charSet);
				logger.info("响应结果"+httpStr);
			}catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
			httpCodeStr = String.valueOf(response.getStatusLine().getStatusCode());
			
			returnMap.put("responseXML", httpStr);
			returnMap.put("responseCode", httpCodeStr);
		}finally {
			if (response != null) {
				try {
					//释放资源
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}finally {
					//关闭CloseableHttpResponse
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			httpPost.releaseConnection();
			//关闭HttpClient
			shutdown();
		}
		return returnMap;
	}
	
	/**
	 * HJ 2016-12-1 10:55:57修改--返回的是map
	 * 黑龙江移动用postHttp
	 * 发送post请求【查询参数使用字符串】，并返回字符串
	 * 
	 * @param url 请求地址
	 * @param requestContext 请求参数字符串
	 * @param token 
	 * @param signatrue 
	 * @return
	 */
	public static Map<String, String> postHttpReturnMap(String url, String requestContext,String token, String signatrue) {
		OutputStream outputStream = null;
		InputStream inputStream = null;
		InputStreamReader inputStreamReader = null;
		BufferedReader reader = null;
		Map<String, String> returnMap = new HashMap<>();
		StringBuffer resultBuffer = new StringBuffer();
		OutputStreamWriter outputStreamWriter = null;
		String tempLine = null;
		try {
			URL localURL = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) localURL.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setConnectTimeout(MAX_TIMEOUT2);
			conn.setReadTimeout(MAX_TIMEOUT3);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Accept", "*/*");
			conn.setRequestProperty("Connection", "Keep-Alive");
			conn.setRequestProperty("Accept-Charset", "UTF-8");
			conn.setRequestProperty("Content-Type", "application/xml");
			if(StringUtils.isNotBlank(token)){
				conn.addRequestProperty("4GGOGO-Auth-Token", token);
            }
            if(StringUtils.isNotBlank(signatrue)){
            	conn.addRequestProperty("HTTP-X-4GGOGO-Signature", signatrue);
            }
            // 设置套接工厂
            
			outputStream = conn.getOutputStream();
			outputStreamWriter = new OutputStreamWriter(outputStream);
			outputStreamWriter.write(requestContext);
			outputStreamWriter.flush();
			inputStream = conn.getInputStream();
			//没有close
			
			inputStreamReader = new InputStreamReader(inputStream,"UTF-8");
			reader = new BufferedReader(inputStreamReader);
			while ((tempLine = reader.readLine()) != null) {
				resultBuffer.append(tempLine);
			}
			String httpCodeStr = String.valueOf(conn.getResponseCode());
			logger.info("http响应状态为" + httpCodeStr);
			String httpStr = resultBuffer.toString();
			logger.info("httpStr状态为" + httpStr);
			returnMap.put("responseXML", httpStr);
			returnMap.put("responseCode", httpCodeStr);
		} catch (Exception e) {//其他异常
			logger.warn("==>Exception异常"+e.toString());
			e.printStackTrace();
		}finally {
			if (outputStreamWriter != null) {
				try {
					outputStreamWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return returnMap;
	}
	
	/**
	 * 使用证书发送post请求
	 * 	目前微信在使用这个方法
	 * @param url 请求地址
	 * @param  请求参数字符串
	 * @param headers 请求头部数组
	 * @param charSet 请参数编码格式
	 * @return
	 */
	public static String SSLPost(String url, String requestContext,Map<String, String> headers, String charSet, String certPath, String certMchId)  throws Exception{
		KeyStore keyStore  = KeyStore.getInstance("PKCS12");
		FileInputStream instream = new FileInputStream(new File(certPath));
		try {
		    keyStore.load(instream, certMchId.toCharArray());
		} finally {
		    instream.close();
		}
		
		// Trust own CA and all self-signed certs
		SSLContext sslcontext = SSLContexts.custom()
		        .loadKeyMaterial(keyStore, certMchId.toCharArray())
		        .build();
		// Allow TLSv1 protocol only
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
		        sslcontext,
		        new String[] { "TLSv1" },
		        null,
		        SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER);
		CloseableHttpClient httpclient = HttpClients.custom()
		        .setSSLSocketFactory(sslsf)
		        .build();
	        
		
		StringEntity stringEntity = new StringEntity(requestContext, charSet);// 解决中文乱码问题
		stringEntity.setContentEncoding(charSet);
		String httpStr = null;
		CloseableHttpResponse response = null;
		HttpPost httpPost = new HttpPost(url);
		httpPost.setEntity(stringEntity);
		httpPost.setHeaders(builderHeader(headers));
		try {
			try {
				/*if(url.toLowerCase().startsWith("https://")){
					response = httpsClient.execute(httpPost);
				}else{
				}*/
				response = httpclient.execute(httpPost);
				logger.info("响应："+response);
			} catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString()+e.toString());
				e.printStackTrace();
			}
			HttpEntity entity = response.getEntity();
			try {
				httpStr = EntityUtils.toString(entity, charSet);
				logger.info("响应结果"+httpStr);
			}catch (Exception e) {//其他异常
				logger.warn("==>Exception异常"+e.toString());
				e.printStackTrace();
			}
		}finally {
			if (response != null) {
				try {
					//释放资源
					EntityUtils.consume(response.getEntity());
				} catch (IOException e) {
					e.printStackTrace();
				}finally {
					//关闭CloseableHttpResponse
					try {
						response.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			httpPost.releaseConnection();
			//关闭HttpClient
			shutdown();
		}
		return httpStr;
	}
	public static void showParams(HttpServletRequest request) {  
        Map map = new HashMap();  
        Enumeration paramNames = request.getParameterNames();  
        while (paramNames.hasMoreElements()) {  
            String paramName = (String) paramNames.nextElement();  
            String[] paramValues = request.getParameterValues(paramName);  
            if (paramValues.length == 1) {  
                String paramValue = paramValues[0];  
                if (paramValue.length() != 0) {  
                    map.put(paramName, paramValue);  
                }  
            }  
        }  
  
        Set<Entry<String, String>> set = map.entrySet();
        //logger.info("------------------------------");  
        for (Entry entry : set) {
        	logger.info("GET请求报文："+entry.getKey() + ":" + entry.getValue());  
        }  
        //logger.info("------------------------------");  
    } 
}

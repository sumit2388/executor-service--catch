package net.one97.contest.audit.util;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;




public class HttpClientUtil {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(HttpClientUtil.class);
	
	private HttpClientUtil(){}
	
	private static org.apache.commons.httpclient.HttpClient httpClient = null;
	
	public static HttpClient getHttpClientInstance(){
		if(httpClient == null){
			synchronized(HttpClientUtil.class){
				if(httpClient == null){
					httpClient = HttpClientUtil.getHttpClient();
				}
			}
		}
		return httpClient;
	}
	
	private static HttpClient getHttpClient(){
		int httpMaxTotConn  = 50;
		int httpTimeOut = 1000;
		int httpRetryCnt  = 1;
		org.apache.commons.httpclient.HttpClient client = null;
			
		MultiThreadedHttpConnectionManager connectionManager = null;
		try{
			httpMaxTotConn = Integer.parseInt(PropertyLoader.getProperty("http.max.connection"));
			httpTimeOut = Integer.parseInt(PropertyLoader.getProperty("http.timeout"));
			//httpRetryCnt = Integer.parseInt(PropertyLoader.getProperty("http.retry.count"));
			httpRetryCnt = 2;
		}catch(NumberFormatException e){
			log.info("NumberFormatException to parse Properties(TotalConection|TimeOut|RetryCount).");
		}catch(Exception e){
			log.info("Exception - ["+e.getMessage()+"] to parse Properties(TotalConection|TimeOut|RetryCount).");
		}
		connectionManager = new MultiThreadedHttpConnectionManager();
		HttpConnectionManagerParams params = new HttpConnectionManagerParams();
		params.setMaxTotalConnections(httpMaxTotConn);
		params.setDefaultMaxConnectionsPerHost(httpMaxTotConn);
		connectionManager.setParams(params);
		client = new org.apache.commons.httpclient.HttpClient(connectionManager);
		
		DefaultHttpMethodRetryHandler retryhandler = new DefaultHttpMethodRetryHandler(httpRetryCnt,false);
		client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, retryhandler);
		client.getParams().setAuthenticationPreemptive(true);
		client.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, httpTimeOut);
		
		return client;
	}
	
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}
}

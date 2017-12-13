package com.zhangyida;

import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;

import com.squareup.okhttp.OkHttpClient;

import android.content.Context;
import android.util.Log;

public class HttpUtil {
	public static final String SERVER_PROTOCAL = "https";
	public static final String SERVER_HOST = "10.2.8.11";
	public static final String SERVER_PORT = "8000";
	
	private static final String KEY_STORE_TYPE_P12 = "PKCS12";//证书类型 固定值
    private static final String KEY_STORE_CLIENT_PATH = "client.p12";//客户端要给服务器端认证的证书
    private static final String KEY_STORE_SERVER_PATH = "server.crt";//客户端验证服务器端的证书库
    private static final String KEY_STORE_PASSWORD = "123456";// 客户端证书密码
    
    private static OkHttpClient okHttpClient;
    
	/**
     * 获取SSLContext
     *
     * @param context 上下文
     * @return SSLContext
     */
    public static SSLContext getSSLContext(Context context) {
        try {
        	//参考 https://developer.android.com/training/articles/security-ssl.html
        	CertificateFactory  certificateFactory = CertificateFactory.getInstance("X.509");
        	//这里导入服务端SSL证书文件
        	InputStream inputStream = context.getAssets().open(KEY_STORE_SERVER_PATH);  
              
        	Certificate  cer = certificateFactory.generateCertificate(inputStream);  
  
            //创建一个证书库，并将证书导入证书库  
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());  
            trustStore.load(null,null);
            trustStore.setCertificateEntry("trust", cer);  
        	
        	
            // 服务器端需要验证的客户端证书
            KeyStore keyStore = KeyStore.getInstance(KEY_STORE_TYPE_P12);
            InputStream ksIn = context.getResources().getAssets().open(KEY_STORE_CLIENT_PATH);
            try {
                keyStore.load(ksIn, KEY_STORE_PASSWORD.toCharArray());
            } catch (Exception e) {
                Log.e("Exception", e.getMessage(), e);
            } finally {
                try {
                    ksIn.close();
                } catch (Exception ignore) {
                }
                try {
                	inputStream.close();
                } catch (Exception ignore) {
                }
            }
            SSLContext sslContext = SSLContext.getInstance("TLS");
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);
            
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("X509");
            keyManagerFactory.init(keyStore, KEY_STORE_PASSWORD.toCharArray());
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
            return sslContext;
        } catch (Exception e) {
            Log.e("tag", e.getMessage(), e);
        }
        return null;
    }
    
    /**
     * 获取SSL认证需要的HttpClient
     *
     * @param context 上下文
     * @return OkHttpClient
     */
    public static OkHttpClient getSSLContextHttp(Context context) {
		if (okHttpClient == null) {
			synchronized (HttpUtil.class) {
				if (okHttpClient == null) {
					okHttpClient = new OkHttpClient();
					SSLContext sslContext = getSSLContext(context);
					if (sslContext != null) {
						okHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
					}
					//设置cookie处理器
					okHttpClient.setCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER));
					//设置服务器HostName校验
					okHttpClient.setHostnameVerifier(new HostnameVerifier() {

						@Override
						public boolean verify(String host, SSLSession paramSSLSession) {
							if (SERVER_HOST.equals(host)) {
								return true;
							}
							return false;
						}
					});
				}
			}
		}
		return okHttpClient;
    }
}

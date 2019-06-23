package com.ggfood.test.ggfood_api.tool;

import android.os.Handler;
import android.os.Message;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

/**
 * Created by user on 2017/7/30.
 *
 * @version 0.91
 *
 */

public class AsyncHttpSender {
    public static final int DEFAULT_READ_TIMEOUT = 3000;
    public static final int DEFAULT_CONNECT_TIMEOUT = 3000;
    public static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36";

    public static final int HANDLE_RESPONSE = 0;
    private static final Executor mExecutor = Executors.newSingleThreadExecutor();
    private static Handler mHandler = new TaskHandler();



    static{
        CookieManager cookieManager = new CookieManager(null,CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
    }
    public static void send(Request request,ResponseListener listener){
        mExecutor.execute(new ConnectionTask(request,listener));
    }
    public static String combineData(String target,Map<String,String> getData){
        StringBuilder builder = new StringBuilder(target.length() + getData.size()*6);
        builder.append(target);
        if(getData.size()>0) {
            builder.append('?');
            for (String str : getData.keySet()) {
                builder.append(str);
                builder.append('=');
                builder.append(getData.get(str));
                builder.append('&');
            }
        }
        return builder.toString();
    }

    public static class Request{
        private HttpURLConnection mConnection;
        private URL url;
        private CharSequence postData;
        public Request(String target) throws IOException {
            this(target,DEFAULT_READ_TIMEOUT,DEFAULT_CONNECT_TIMEOUT);
        }
        public Request(String target,int readTimeout,int connectTimeout) throws IOException {
            url = new URL(target);
            mConnection = (HttpURLConnection)url.openConnection();
            mConnection.setReadTimeout(readTimeout);
            mConnection.setConnectTimeout(connectTimeout);
            mConnection.addRequestProperty("User-Agent", DEFAULT_USER_AGENT);
            mConnection.setInstanceFollowRedirects(true);
            mConnection.setDoInput(true);
            mConnection.setDoOutput(false);
        }

        /**
         * 是否自動轉址
         * @param enable 預設為true
         */
        public void setInstanceFollowRedirects(boolean enable){
            mConnection.setInstanceFollowRedirects(enable);
        }
        public void addRequestProperty(String key,String value){
            mConnection.addRequestProperty(key,value);
        }
        public void setPostData(CharSequence data){
            postData = data;
            mConnection.setDoOutput(true);
        }

        /**
         *  key,value,key,value,key,value......................
         * @param pair
         */
        public void setPostData(CharSequence... pair){
            if(pair.length % 2 != 0){
                throw new IllegalArgumentException("");
            }
            StringBuilder sb = new StringBuilder("");
            for(int i=0;i<pair.length;i+=2){
                sb.append(pair[i]);
                sb.append("=");
                sb.append(pair[i+1]);
                sb.append("&");
            }
            sb.deleteCharAt(sb.length()-1);
            postData = sb.toString();
            mConnection.setDoOutput(true);
        }
    }
    /**
     * 用於處理網頁回應的事件
     * onResponse 在其他執行緒中執行
     * onFinish     在UI執行緒中執行
     */
    public interface ResponseListener{
        Object onResponse(int responseCode, Map<String, List<String>> headers, InputStream is);

        void onFinish(int responseCode, Map<String, List<String>> headers, Object data);
    }

    /**
     * 負責送出及在接收訊息時呼叫Listener
     */
    private static class ConnectionTask implements Runnable{
        Request mRequest;
        ResponseListener mResponseListener;

        boolean isOver = false;
        public ConnectionTask(Request request,ResponseListener listener) {
            mRequest = request;
            mResponseListener = listener;
        }

        @Override
        public void run() {
            try {
                HttpURLConnection conn = mRequest.mConnection;

                //若無設定DoOutput則不輸出postData
                if(conn.getDoOutput()) {
                    byte[] data = mRequest.postData.toString().getBytes("utf-8");
                    conn.addRequestProperty("Content-Length", String.valueOf(data.length));
                    BufferedOutputStream dataOS = new BufferedOutputStream(conn.getOutputStream());
                    dataOS.write(data);
                    dataOS.close();
                }
                if(mResponseListener != null) {
                    InputStream is = mRequest.mConnection.getInputStream();
                    //處理gzip
                    if ("gzip".equals(conn.getContentEncoding())) {
                        is = new GZIPInputStream(is);
                    }

                    ResultSet result = new ResultSet();
                    result.listener = mResponseListener;
                    result.headers = conn.getHeaderFields();
                    CookieHandler.getDefault().put(conn.getURL().toURI(), result.headers);
                    result.obj = mResponseListener.onResponse(conn.getResponseCode(), result.headers, is);
                    Message msg = mHandler.obtainMessage(HANDLE_RESPONSE,conn.getResponseCode(),0,result);
                    mHandler.sendMessage(msg);
                }else{
                    conn.connect();
                }
            } catch (IOException | URISyntaxException e) {
                e.printStackTrace();
            }
            isOver = true;
        }
    }
    private static class ResultSet {
        ResponseListener listener;
        Object obj;
        Map<String,List<String>> headers;
    }
    private static class TaskHandler extends Handler{
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case HANDLE_RESPONSE:
                    ResultSet res = (ResultSet)msg.obj;
                    res.listener.onFinish(msg.arg1,res.headers,res.obj);
                    break;
            }
        }
    }
}

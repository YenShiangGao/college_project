package com.ggfood.test.ggfood_api.tool;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**`
 * Created by Wei on 2017/4/6.
 */

public class Http {
    public String url;
    public Handler httpHandler;
    public HashMap<String,String> getParameter;

    Thread thread;

    public int readTimeout = 10000;
    public int connectTimeout = 15000;


    public Http(String url){
        this.url = url;
        getParameter = new HashMap<String,String>();
    }
    public Http(String url, Handler httpHandler){
        this.url = url;
        getParameter = new HashMap<String,String>();
        setHandler(httpHandler);
    }
    public void setHandler(Handler httpHandler){
        this.httpHandler = httpHandler;
    }
    public void addParameter(String k,String v){
        getParameter.put(k,v);
    }
    public void removeParameter(String k){
        getParameter.remove(k);
    }
    public void send(){
        if(thread == null) {
            thread = new Thread(requestTask);
        }else if(!thread.isAlive()){
            thread = new Thread(requestTask);
        }
        thread.start();

    }
    private Runnable requestTask = new Runnable(){
        public void run(){


            HttpURLConnection conn = null;
            try {
                if (Thread.interrupted()) {
                    return;
                }
                // 建立連線
//                StringBuilder  = Http.this.url;
//                if(u.charAt(u.length()-1) != '?' && getParameter.size()>0){
//
//
//                }
                Log.e("RequestTask","send : "+Http.this.url);
                URL url = new URL(Http.this.url);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(readTimeout);
                conn.setConnectTimeout(connectTimeout);
                conn.setRequestMethod("GET");
                conn.connect();
                if (Thread.interrupted()) {
                    return;
                }
                // 讀取資料
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        conn.getInputStream(), "UTF-8"));

                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while((line = reader.readLine()) != null){
                    stringBuilder.append(line);
//                    Log.e("RequestTask","r:"+line);
                }
                reader.close();
               // Log.e("RequestTask","code : "+conn.getResponseCode()+",receive : "+stringBuilder.toString());
                Message msg = new Message();
                msg.what = 1;
                Bundle bundle = new Bundle();
                bundle.putString("html",stringBuilder.toString());
                msg.setData(bundle);
                httpHandler.sendMessage(msg);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    };
}

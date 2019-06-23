package com.ggfood.test.ggfood_api.tool;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.DrawableRes;
import android.util.AttributeSet;
import android.util.Log;

import com.ggfood.test.ggfood_api.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by s121 on 2017/4/11.
 */

public class WebImageView extends android.support.v7.widget.AppCompatImageView {
    private static Drawable emptyDrawable = null;
    private Uri webUri = null;
    private Drawable webImage = null;
    private Thread loadingThread = null;
    private Runnable loadingTask = new Runnable(){
        @Override
        public void run() {
            Log.e("loadingThread","start");
            if(webUri != null){
                try {
                    Log.e("loadingThread","load : " + webUri.toString());
                    URL url = new URL(webUri.toString());
                    URLConnection connection = url.openConnection();
                    InputStream is = connection.getInputStream();
                    BitmapDrawable bitmap = new BitmapDrawable(getContext().getResources(),is);
                    webImage = bitmap;
                    decodeHandler.sendEmptyMessage(0);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (InterruptedIOException e) {
                    //被終止不做任何事
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    private Handler decodeHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(webImage != null){
                setImageDrawable(webImage);
            }
        }
    };

    public WebImageView(Context context) {
        this(context,null,0);
    }

    public WebImageView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public WebImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        webUri = Uri.parse("");
        loadingThread = new Thread(loadingTask);
        Log.e("WebImageView","create");
    }
    public void setImageURI(String uri) {
        setImageURI(Uri.parse(uri));
    }
    @Override
    public void setImageURI(Uri uri) {
        if(emptyDrawable == null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                emptyDrawable = getContext().getResources().getDrawable(R.drawable.i,getContext().getTheme());
            }else{
                emptyDrawable = getContext().getResources().getDrawable(R.drawable.i);
            }
        }
//        if(!webUri.equals(uri)) {            //指定的Uri和當前的不同
//            Log.e("setImageURI", "set : " + uri.toString());
//            webUri = uri;
//            this.setImageDrawable(emptyDrawable);
//        }
        if(!webUri.equals(uri)){            //指定的Uri和當前的不同
            Log.e("setImageURI","set : " + uri.toString());
            webUri = uri;
            this.setImageDrawable(emptyDrawable);
            if (!loadingThread.isAlive()) {                 //if 正在載入
                loadingThread = new Thread(loadingTask);
                loadingThread.start();                      //開始載入
            }else {
                loadingThread.interrupt();                  //終止當前的載入程序
                try {
                    loadingThread.join();                   //等待終止完成
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                // 終止完成後  重新載入新的圖片
                loadingThread = new Thread(loadingTask);
                loadingThread.start();
            }
        }
    }

    @Override
    public void setImageResource(@DrawableRes int resId) {
        super.setImageResource(resId);
    }
}

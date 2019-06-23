package com.ggfood.test.ggfood_api.tool;

import android.os.Handler;
import android.util.Log;

import com.ggfood.test.ggfood_api.STTService;

public class GGTimer {

    Handler handler = new Handler();
    Thread thread = new Thread(new Task());

    private OnSuccess defaultOnSuccess = new OnSuccess() {
        @Override
        public void run() {
            mService.speak("時間到囉");
        }
    };

    OnSuccess onSuccess = defaultOnSuccess;
    STTService mService;

    int hour;
    int minute;
    int second;
    long diffTime;

    public void setDiffTime(long diffTime) {
        this.diffTime = diffTime;
    }

    public GGTimer(STTService service ,int hour, int minute, int second) {
        mService = service;
        this.hour = hour;
        this.minute = minute;
        this.second = second;
        diffTime = (second+minute*60+hour*60*60)*1000;
    }

    public GGTimer(STTService service ,long diffTime) {
        mService = service;
        int sec = (int)(diffTime /1000);
        minute = sec/60;
        second = sec%60;
        hour = minute/60;
        minute = minute%60;
        this.diffTime = diffTime;
    }
    public GGTimer(STTService service){
        mService = service;
        minute = 1;
        this.diffTime = 60*1000;
    }

    public void setOnSuccess(OnSuccess onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void start(){
        thread.start();
    }

    public void stop(){
        thread.interrupt();
    }

    public void reset(){
        if(thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        thread = new Thread(new Task());
    }
    private class Task implements Runnable{
        @Override
        public void run() {
            if(onSuccess == null) return;
            Log.e("GGTimer","run");
            try {
                long current = System.currentTimeMillis();
                long target = current + diffTime;
                long d = 1000;
                for(;current + 1000 > target;){
                    current = System.currentTimeMillis();
                        Thread.sleep(d);
                    d = 1000 - (System.currentTimeMillis() - current);
                }
                d = target - System.currentTimeMillis();
                if(d > 0)
                    Thread.sleep(d);
                handler.post(onSuccess);
            } catch (InterruptedException e) {
                return;
            }
        }
    }



    public interface OnSuccess extends Runnable{
        void run() ;
    }

}

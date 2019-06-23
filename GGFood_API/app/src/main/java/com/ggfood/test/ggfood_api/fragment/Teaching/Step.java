package com.ggfood.test.ggfood_api.fragment.Teaching;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ggfood.test.ggfood_api.R;
import com.ggfood.test.ggfood_api.Teaching;
import com.ggfood.test.ggfood_api.fragment.BaseFragment;
import com.ggfood.test.ggfood_api.tool.WebImageView;

import org.json.JSONArray;
import org.json.JSONException;

/**
 * Created by Wei on 2017/5/23.
 */

public class Step extends BaseFragment{
    public static final String TAG = "Step";
    public TextView textView1,textView2;
    public String steps[];
    public String img[];
    public int page;
    private boolean viewIsCreated = false;
    public WebImageView imageView;
    Teaching activity;
    public static Step newInstance(JSONArray foodList,int page){
        Step step = new Step();
        Bundle pageBundle = new Bundle();
        pageBundle.putInt("page",page);
        step.setArguments(pageBundle);
        step.steps=new String[foodList.length()];
        step.img=new String[foodList.length()];
        for(int i=0;i<foodList.length();i++){
            try {
                step.steps[i]=foodList.getJSONObject(i).getString("text");
                try {
                    step.img[i] = foodList.getJSONObject(i).getString("img");
                }catch(Exception e){

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return step;
  }
//    /************************************************/
//    public static STTService.MyBinder binder = null;
//    Toast toast;
//    private void showToast(String msg){
//        toast.setText(msg);
//        toast.show();
//    }
//    private ServiceConnection mConnection = new ServiceConnection() {
//        @Override
//        public void onServiceConnected(ComponentName className,
//                                       IBinder service) {
//            Log.e(TAG,"onServiceConnected");
//            binder = ((STTService.MyBinder)service);
//            binder.registerListener(Step.this);
//            changeText(1);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            Log.e(TAG,"onServiceDisconnected");
//            binder.unregisterListener(Step.this);
//            binder = null;
//        }
//    };
//    /************************************************/
    public void changeText(int i){

        if(getActivity() != null)activity.binder.startRecord();
        Log.e("changeText","i:"+i);
        page = i-1;
        if(viewIsCreated) {
            if (page < steps.length) {
                textView1.setText("Step " + i + "/" + steps.length);
                textView2.setText(steps[page]);
                if(img[page]!=null && !img[page].equals("null")){
                    imageView.setImageURI(img[page]);
                }
//                speak("第" + i + "步驟。" + steps[page]);
            }
        }
    }
    @Override
    protected void initView(View rootView) {
        activity = (Teaching)getActivity();
        Log.e("Step","initView");
        textView1=(TextView)rootView.findViewById(R.id.step);
        textView2=(TextView)rootView.findViewById(R.id.text);
        imageView=(WebImageView)rootView.findViewById(R.id.imageView);

        viewIsCreated = true;
        changeText(getArguments().getInt("page",1));
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.e("Step","onDestroyView");
        viewIsCreated = false;
    }

    @Override
    protected int getMainLayoutId() {
        return R.layout.teaching_step;
    }



    @Override
    public void onResume() {
        super.onResume();
//        Intent it = new Intent(getActivity(),STTService.class);
//        if(!getActivity().bindService(it,mConnection, Context.BIND_AUTO_CREATE))
//            showToast("bind service is failed");
        Log.e(TAG,"onResume");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        getActivity().unbindService(mConnection);
    }

    public void speak(String str){
        if(str.equals(""))
            ((Teaching)getActivity()).binder.stopTTS();
        if(getActivity() != null)
            ((Teaching)getActivity()).binder.startSpeak(str);
    }
    public boolean TTSisSpeaking(){
        if(getActivity() != null)
            return ((Teaching)getActivity()).binder.isSpeaking();
        return true;
    }
    @Override
    public void onOutterActivityCall(Object data) {
        Log.e("onOutterActivityCall","" + TTSisSpeaking());
        String str = data.toString();
        switch(str){
            case "toggle":
                if(TTSisSpeaking()) {
                    speak("");
                }else{
                    speak("第" + (page + 1) + "步驟。" + steps[page]);
                }

                break;
            case "speak":
                speak("第" + (page + 1) + "步驟。" + steps[page]);
                break;
        }
    }
}

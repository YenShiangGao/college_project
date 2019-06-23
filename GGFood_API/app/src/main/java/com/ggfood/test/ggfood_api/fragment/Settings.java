package com.ggfood.test.ggfood_api.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.Config;
import com.ggfood.test.ggfood_api.R;
import com.ggfood.test.ggfood_api.STTService;

import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */



public class Settings extends BaseFragment implements STTService.STTEvent {
    String TAG= "Settings";
    SeekBar rate,pitch;
    EditText host;
    Button save;
    SharedPreferences settings;
    /************************************************/
    public STTService.MyBinder binder = null;
    Toast toast;
    private void showToast(String msg){
        toast.setText(msg);
        toast.show();
    }
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.e(TAG,"onServiceConnected");
            binder = ((STTService.MyBinder)service);
            binder.registerListener(Settings.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG,"onServiceDisconnected");
            binder.unregisterListener(Settings.this);
            binder = null;
        }
    };
    /************************************************/
    @Override
    protected void initView(View rootView) {
        rate=(SeekBar)rootView.findViewById(R.id.seekBar_rate);
        pitch=(SeekBar)rootView.findViewById(R.id.seekBar_pitch);
        host=(EditText)rootView.findViewById(R.id.editText_host);
        save=(Button)rootView.findViewById(R.id.btn_save);
        settings = getActivity().getSharedPreferences("Config",0);
        Config.HOSTURL=settings.getString("HOSTURL","wei18963.ddns.net");
        Config.pitch=settings.getFloat("PITCH",1.3f);
        Config.rate=settings.getFloat("RATE",1.5f);
        rate.setMax(200);
        rate.setProgress((int)(Config.rate*100));
        pitch.setMax(200);
        pitch.setProgress((int)(Config.pitch*100));
        host.setText(Config.HOSTURL);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
            }
        });
    }
    public void saveSettings(){

        Config.HOSTURL=host.getText().toString();
        Config.pitch=(float)pitch.getProgress()/100;
        Config.rate=(float)rate.getProgress()/100;
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("HOSTURL", Config.HOSTURL);
        editor.putFloat("PITCH",  Config.pitch);
        editor.putFloat("RATE",  Config.rate);

        editor.commit();
        binder.startSpeak("已儲存您的設定");
        binder.setVoice();
    }
    @Override
    protected int getMainLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    public void onSTTResult(JSONObject result) {

    }

    @Override
    public void onStartRecord() {

    }

    @Override
    public void onStopRecord() {

    }
    @Override
    public void onResume() {
        super.onResume();
        Intent it = new Intent(getActivity(),STTService.class);
        if(!getActivity().bindService(it,mConnection, Context.BIND_AUTO_CREATE))
            showToast("bind service is failed");
        Log.e(TAG,"onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mConnection);
    }

}

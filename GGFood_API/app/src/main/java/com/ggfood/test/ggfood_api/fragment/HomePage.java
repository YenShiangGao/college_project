package com.ggfood.test.ggfood_api.fragment;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.R;
import com.ggfood.test.ggfood_api.STTService;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomePage extends BaseFragment implements STTService.STTEvent{
    public static final String TAG="HomePage";
    public STTService.MyBinder binder = null;
    Toast toast;
    TextView textView;

    @Override
    protected void initView(View rootView) {
        /************************************************/
        toast = Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT);
        /************************************************/
        textView=(TextView)rootView.findViewById(R.id.textView);
    }
    /************************************************/
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.e(TAG,"onServiceConnected");
            binder = ((STTService.MyBinder)service);
            binder.registerListener(HomePage.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG,"onServiceDisconnected");
            binder.unregisterListener(HomePage.this);
            binder = null;
        }
    };
    /************************************************/
    @Override
    protected int getMainLayoutId() {
        return R.layout.fragment_home_page;
    }

    @Override
    public void onSTTResult(JSONObject result) {
        try {
            textView.setText(result.getString("Speech"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
    public void onPause() {
        super.onPause();
        super.onPause();
        getActivity().unbindService(mConnection);
    }
    private void showToast(String msg){
        toast.setText(msg);
        toast.show();
    }
}

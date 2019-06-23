package com.ggfood.test.ggfood_api.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Wei on 2017/5/9.
 */

public abstract class BaseFragment extends Fragment {
    public static BaseFragment mInstance = null;
    public BaseFragment(){
        mInstance = this;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(getMainLayoutId(), container, false);
        initView(v);
        return v;
    }
    public static BaseFragment getInstance(){
        return mInstance;
    }
    protected abstract void initView(View rootView);
    protected abstract int getMainLayoutId();
    public void onOutterActivityCall(Object data){}
    public void sendData(Object data){
        onOutterActivityCall(data);
    }

}

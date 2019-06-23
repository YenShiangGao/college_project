package com.ggfood.test.ggfood_api.fragment.Teaching;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.ggfood.test.ggfood_api.R;
import com.ggfood.test.ggfood_api.Teaching;
import com.ggfood.test.ggfood_api.fragment.BaseFragment;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Wei on 2017/5/23.
 */

public class Food extends BaseFragment{
    public static final String TAG = "Food";
    public static String [] foodName;
    public static String [] foodCount;
    String speak;
    private ListView listView;
    private SimpleAdapter simpleAdapter;

    Teaching activity;
    public static Food newInstance(JSONArray foodList){
        Food food = new Food();
        foodName=new String[foodList.length()];
        foodCount=new String[foodList.length()];
        for(int i=0;i<foodList.length();i++){
            try {
                foodName[i]=foodList.getJSONObject(i).getString("name");
                foodCount[i]=foodList.getJSONObject(i).getString("weight");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return food;
    }
    /************************************************/
//    public STTService.MyBinder binder = null;
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
//            binder.registerListener(Food.this);
//            binder.startSpeak(speak);
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName arg0) {
//            Log.e(TAG,"onServiceDisconnected");
//            binder.unregisterListener(Food.this);
//            binder = null;
//        }
//    };
    /************************************************/
    @Override
    protected void initView(View rootView) {
        activity = (Teaching)getActivity();
        listView=(ListView)rootView.findViewById(R.id.food_list);
        List<Map<String, String>> items = new ArrayList<Map<String,String>>();
        for(int i=0;i<foodName.length;i++){
            Map<String, String> item = new HashMap<String, String>();
            item.put("name",foodName[i]);
            item.put("weight",foodCount[i]);
            items.add(item);
        }
        simpleAdapter = new SimpleAdapter(getActivity(),
                items,R.layout.food_listview_style,new String[]{"name","weight"},new int[]{R.id.history_name,R.id.weight});
        listView.setAdapter(simpleAdapter);
        speak="";
//        activity.binder.startSpeak(speak);
        activity.binder.startSpeak(speak);
        activity.binder.textRequest("Teaching_Ingredients");

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return getActivity().onTouchEvent(event);
            }
        });
        listView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }
    public void speakIngredients(){
        speak="先跟您確認食材。";
        for(int i=0;i<foodName.length;i++){
            speak+=foodName[i]+"。"+foodCount[i]+"。";
        }
        speak+="請問您準備好了嗎?";
        activity.binder.startSpeak(speak);
    }

    @Override
    protected int getMainLayoutId() {
        return R.layout.teaching_food;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e(TAG,"onResume");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

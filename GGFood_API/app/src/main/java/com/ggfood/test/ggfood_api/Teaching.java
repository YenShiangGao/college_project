package com.ggfood.test.ggfood_api;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.database.FoodDataBase;
import com.ggfood.test.ggfood_api.fragment.BaseFragment;
import com.ggfood.test.ggfood_api.fragment.Teaching.Food;
import com.ggfood.test.ggfood_api.fragment.Teaching.Step;
import com.ggfood.test.ggfood_api.tool.AsyncHttpSender;
import com.ggfood.test.ggfood_api.tool.GGTimer;
import com.google.gson.JsonElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Teaching extends AppCompatActivity implements STTService.STTEvent {
    public static final String TAG = "Teaching";
//    public static Teaching instance;
    private FoodDataBase db;
    private int total_page = 0;
    private int page = 0;
    private FragmentManager fragmentManager = getFragmentManager();
    private Food foodFragment;
    private Step stepFragment;
    private BaseFragment currentFragment;
    public FragmentTransaction transaction;
    public float downX,downY,upX,upY;
    public static int EAST=1,WEST=0;
    public int mDirection,mNextDirection;
    JSONObject recipeData;
    JSONArray ingredients;
    JSONArray steps;
    AlertDialog alertDialog;

    String[] names;
    boolean[] alertDialog_checked;
    private Handler handler = new Handler();
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
            binder.registerListener(Teaching.this);

            currentFragment = foodFragment = Food.newInstance(ingredients);
//            stepFragment = Step.newInstance(steps,1);

            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.teaching_frame,currentFragment,foodFragment.getClass().getSimpleName());
            transaction.commit();
            binder.startRecord();
//            changeTo(0); //食材
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG,"onServiceDisconnected");
            binder.unregisterListener(Teaching.this);
            binder = null;
        }
    };
    /************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teaching);
//        instance=this;
        Intent intent=getIntent();
        String data=intent.getStringExtra("recipeData");
        try {
            recipeData=new JSONObject(data);
            ingredients=recipeData.getJSONArray("ingredients");
            steps=recipeData.getJSONArray("steps");
            total_page=steps.length();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        db = FoodDataBase.getDB(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:{
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_MOVE:{

                return true;
            }
            case MotionEvent.ACTION_UP:{
                Log.d("onTouchEvent-ACTION_UP","UP");
                upX = event.getX();
                upY = event.getY();
                float x=Math.abs(upX-downX);
                float y=Math.abs(upY-downY);

                double z=Math.sqrt(x*x+y*y);
                int jiaodu=Math.round((float)(Math.asin(y/z)/Math.PI*180));//角度

                if(z >= 50){
                    if (upY < downY && jiaodu>45) {//上
                        Log.d("onTouchEvent-ACTION_UP","角度:"+jiaodu+", 動作:上");
                    }else if(upY > downY && jiaodu>45) {//下
                        Log.d("onTouchEvent-ACTION_UP","角度:"+jiaodu+", 動作:下");
                    }else if(upX < downX && jiaodu<=45) {//左
                        Log.d("onTouchEvent-ACTION_UP","角度:"+jiaodu+", 動作:左");
                        // 原方向不是向右時，方向轉右
                        nextStep();
                        if (mDirection != EAST) {
                            mNextDirection = WEST;
                        }
                    }else if(upX > downX && jiaodu<=45) {//右
                        Log.d("onTouchEvent-ACTION_UP","角度:"+jiaodu+", 動作:右");
                        backStep();
                        // 原方向不是向左時，方向轉右
                        if (mDirection != WEST) {
                            mNextDirection = EAST;
                        }
                    }
                }else {
                    //TODO 加入點擊事件
                    if(z <= 50){
                        Log.d("onTouchEvent-ACTION_UP","toggle");
                        currentFragment.sendData("toggle");
                    }
                }
                return true;
            }

        }
        return super.dispatchTouchEvent(event);
    }
    public void repeatStep(){
        changeTo(page);
    }
    public void nextStep(){
        changeTo(page + 1);
    }
    public void backStep(){
        changeTo(page-1);
    }
    public void changeTo(int n){
        if(!this.isDestroyed()) {
            if (n <= total_page && n >= 0) {
                //page  為現在頁數
                //n     為目標頁數
                if (page == n) return;
                Log.e("changeTo", n + "");
//        if(n==0){
//            binder.stopTTS();
//        }
                if (page >= 1 && n == 0) {
                    transaction = fragmentManager.beginTransaction();
                    transaction.show(foodFragment);
                    transaction.hide(stepFragment);
                    transaction.commit();
                    currentFragment = foodFragment;
                } else if (page == 0 && n >= 1) {
                    transaction = fragmentManager.beginTransaction();
                    transaction.hide(foodFragment);
                    if (stepFragment == null) {
                        stepFragment = Step.newInstance(steps, n);
                        transaction.add(R.id.teaching_frame, stepFragment);
                    } else {
                        transaction.show(stepFragment);
                        stepFragment.changeText(n);
                    }
                    transaction.commit();
                    currentFragment = stepFragment;
                } else {
                    stepFragment.changeText(n);
                }
                page = n;
            }
        }
    }

    @Override
    public void onSTTResult(JSONObject result) {

        try {
            Log.e(TAG,result.getString("action").toString());
            switch(result.getString("action").toString()){
                case "recommend":{
                    finish();
                    break;
                }
                case  "Teaching_ingredients_ok":{
                    foodFragment.speakIngredients();
                    break;
                }
                case "open_history":{
                    finish();
                    break;
                }
                case "open_scan_activity":{
                    finish();
                    break;
                }
                case "cooking_down":{
                    nextStep();
                    currentFragment.sendData("speak");
//                    currentFragment.onOutterActivityCall("speak");
                    break;
                }
                case "cooking_up":{
                    backStep();
                    currentFragment.sendData("speak");
                    break;
                }
                case "cooking_select_page":{
                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
                    int number = params.get("number").getAsInt();
                    Log.e("COOKING","n:"+number);
                    changeTo(number);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            currentFragment.sendData("speak");
                        }
                    },50);
                    break;
                }
                case "addWhat":{
                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
                    for(int i=0; i< ingredients.length();i++){
                        StringBuilder sb = new StringBuilder(params.get("any").toString().length());
                        if(params.get("any").toString().length() > 1) {
                            sb.append(params.get("any").toString(), 1, params.get("any").toString().length() - 1);
                        }
                        Log.e("addWhat",ingredients.getJSONObject(i).getString("name")+":"+sb.toString());
                        if(ingredients.getJSONObject(i).getString("name").equals(sb.toString())){
                           binder.startSpeak(ingredients.getJSONObject(i).getString("name")+"要加"+ingredients.getJSONObject(i).getString("weight"));
                        }
                    }
                    break;
                }
                case "call_Timer":{
                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
                    int number = params.get("number").getAsInt();
                    String unit = params.get("time-unit").getAsString();
                    Log.e("call timer ",number+unit);
                    binder.startSpeak("開始計時"+ number + unit);
                    if(unit.equals("分"))
                        number*=60;
                    else if(unit.equals("小時"))
                        number*=60*60;
                    GGTimer ggTimer = binder.getTimer();
                    ggTimer.reset();
                    ggTimer.setDiffTime(number*1000);
                    ggTimer.start();

                    break;
                }
                //詢問冰箱缺什麼
                case "icebox_find_lost":{
                    if(!isFinishing()) {
                        String[] items = db.getIceBoxTable().findNotExist(Food.foodName);
                        Log.e("String[]", StrArrayToString(Food.foodName));
                        Log.e("String[] ITEM", StrArrayToString(items));
                        if (items.length == 0) {
                            binder.startSpeak("冰箱都有喔");
                        } else {
//                        Intent intent = new Intent(this, PreviewOrder.class);
//                        intent.putExtra(PreviewOrder.ITEM_FEILD, items);
//                        startActivity(intent);
                            StringBuilder builder = new StringBuilder(items.length * 4);
                            builder.append("缺了");
                            for (int i = 0; i < items.length - 1; i++) {
                                if(items[i] != null) {
                                    builder.append(items[i]);
                                    builder.append('，');
                                }
                            }
                            if(items[items.length - 1] != null)
                                builder.append('和').append(items[items.length - 1]);
                            builder.append("    需要幫你訂購嗎");
                            names = items;
                            binder.startSpeak(builder.toString());
                            showDialog();
                        }
                    }
                    break;
                }
                case "order_confirm":{
                    if(alertDialog != null && alertDialog.isShowing()){
                        orderSubmit();
                        alertDialog.dismiss();
                    }
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private String StrArrayToString(String[] arr){
        StringBuilder builder;
        int len = 0;
        builder  = new StringBuilder(len);
        for(int i=0;i<arr.length;i++){
            builder.append('\n').append(arr[i]);
        }
        return builder.toString();
    }
    @Override
    public void onStartRecord() {

    }

    @Override
    public void onStopRecord() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent it = new Intent(this,STTService.class);
        if(!bindService(it,mConnection, Context.BIND_AUTO_CREATE))
            showToast("bind service is failed");
        Log.e(TAG,"onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        binder.stopTTS();
        binder.stopRecord();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    private void showDialog(){

        alertDialog_checked = new boolean[Food.foodName.length];
        String foods="";
        for(int i=0;i<Food.foodName.length;i++){
            foods+="name LIKE "+"'%"+Food.foodName[i]+"'";
            if(i!=Food.foodName.length-1){
                foods += " OR ";
            }
        }
        Log.e("SQL",foods);
        String url="http://"+Config.HOSTURL+"/ggfood/shop/query/product_search_all.php";
        try {
            AsyncHttpSender.Request request = new AsyncHttpSender.Request(url);
            request.setPostData("name",foods);
            AsyncHttpSender.send(request,
                    new AsyncHttpSender.ResponseListener() {
                        @Override
                        public Object onResponse(int responseCode, Map<String, List<String>> headers, InputStream is) {
                            try {
                                BufferedReader in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                                String response;
                                StringBuilder sb = new StringBuilder(20);
                                String v;
                                while((v = in.readLine()) != null) {
                                    sb.append(v);
                                }
                                response = sb.toString();
                                Log.e("onResponse",response);
                                try {
                                    JSONObject jsonobject = new JSONObject(response);
                                    JSONArray jsonarray= jsonobject.getJSONArray("results");
                                    return jsonarray;
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }
                        @Override
                        public void onFinish(int responseCode, Map<String, List<String>> headers, Object data) {
                            if (data != null){
                                try {
                                    JSONArray arr = (JSONArray)data;
                                    String[] items = itemCombine(arr);
                                    for(int i=0;i<names.length;i++){
                                        for(int j=0;j<Food.foodName.length;j++){
                                            Log.e("最後ㄌ","i:"+i+",j:"+j+"food:"+Food.foodName[j]+"name:"+names[i]);
                                            if(Food.foodName[j].equals(names[i])){
                                                Log.e("最後ㄌ","相同");
                                                alertDialog_checked[j] = true;
                                                break;
                                            }
                                        }
                                    }

                                    alertDialog = new AlertDialog.Builder(Teaching.this)
                                        .setMultiChoiceItems(items, alertDialog_checked,
                                                new DialogInterface.OnMultiChoiceClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                        alertDialog_checked[which] = isChecked;
                                                    }
                                                })
                                        .setTitle("請勾選要訂購的食材")
                                        .setPositiveButton("送出訂單",new DialogInterface.OnClickListener(){
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                orderSubmit();
                                            }
                                        })
                                        .setNeutralButton("取消", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        })
                                        .setCancelable(true)
                                        .show();
                                }catch (JSONException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void sendOrderRequest(String[] names,int[] quantity){
        SharedPreferences sharedPreferences = this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Log.e("sendOrderRequest","run"+sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF,false));
        if(sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF,false)){
            String m_id="";
            String address="";
            String phone="";
            String url="";
            try {
                m_id = sharedPreferences.getString("account_id","");
                address = sharedPreferences.getString("address", "");
                phone = sharedPreferences.getString("phone", "");
                url = "http://" + Config.HOSTURL + "/ggfood/shop/query/order_insert_byname.php?id=" + m_id;
                Log.e("sendOrderRequest",
                        "測試" +
                                address + "\n" +
                                phone + "\n" +
                                url);
            }catch(Exception e){
                e.printStackTrace();
            }
            try {
                AsyncHttpSender.Request request = new AsyncHttpSender.Request(url);

                request.setPostData(
                        "address",address,
                        "payment_method","1",
                        "delivery_method","0",
                        "phone",phone,
                        "note","自動訂購",
                        "names",parseStringArray(names).trim(),
                        "quantity",parseStringArray(quantity).trim()
                );
                AsyncHttpSender.send(request,
                        new AsyncHttpSender.ResponseListener() {
                            @Override
                            public Object onResponse(int responseCode, Map<String, List<String>> headers, InputStream is) {
                                try {
                                    BufferedReader in = new BufferedReader(new InputStreamReader(is,"UTF-8"));
                                    String response;
                                    StringBuilder sb = new StringBuilder(20);
                                    String v;
                                    while((v = in.readLine()) != null) {
                                        sb.append(v);
                                    }
                                    response = sb.toString();
                                    Log.e("onResponse",response);
                                    JSONObject jsonObject =new JSONObject(response);
                                    binder.startSpeak(jsonObject.getString("msg"));
                                    return jsonObject.getString("msg");
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                            @Override
                            public void onFinish(int responseCode, Map<String, List<String>> headers, Object data) {
                                if(data != null)
                                    Toast.makeText(Teaching.this,data.toString(),Toast.LENGTH_SHORT).show();
                            }
                        });
            }catch (IOException e){
                e.printStackTrace();
            }

        }else{
            Toast.makeText(this,"請先登入",Toast.LENGTH_SHORT).show();
        }


    }

    private String parseStringArray(String[] str){
        StringBuilder sb = new StringBuilder(str.length*4);
        sb.append(str[0]);
        for(int i=1;i<str.length;i++){
            sb.append(',');
            sb.append(str[i]);
        }
        return sb.toString();
    }
    private String parseStringArray(int[] str){
        StringBuilder sb = new StringBuilder(str.length*4);
        sb.append(String.valueOf(str[0]));
        for(int i=1;i<str.length;i++){
            sb.append(',');
            sb.append(String.valueOf(str[i]));
        }
        return sb.toString();
    }
    private int getCheckedCount(boolean[] arr){
        int sum = 0;
        for(int i=0;i<arr.length;i++)
            if(arr[i])sum+=1;
        return sum;
    }
    private void orderSubmit(){
        binder.startSpeak("訂單已送出");
        Toast.makeText(Teaching.this,"訂單已送出",Toast.LENGTH_LONG);
        int checkedCount = getCheckedCount(alertDialog_checked);
        String[] checkedItem = new String[checkedCount];
        int pos = 0;
        for(int i=0;i<Food.foodName.length;i++){
            if(alertDialog_checked[i]) {
                checkedItem[pos] = Food.foodName[i];
                pos += 1;
            }
        }
        int[] qq = new int[checkedItem.length];
        for(int i=0;i<qq.length;i++)
            qq[i] = 1;
        sendOrderRequest(checkedItem,qq);
    }
    private String[] itemCombine(JSONArray array) throws JSONException {
        try {
            ArrayList<String> result = new ArrayList(array.length());
            for (int i = 0; i < Food.foodName.length; i++) {
                for (int j = 0; j < array.length(); j++) {
                    JSONObject item = array.getJSONObject(j);
                    String name = item.getString("name");
                    String price = item.getString("price");
                    String unit = item.getString("unit");
                    if (Food.foodName[i].equals(name)) {
                        StringBuilder sb = new StringBuilder(name.length() + price.length() + unit.length());
                        sb.append(name).
                                append("      ").
                                append(price).
                                append("$/").
                                append(unit);
                        result.add(sb.toString());
                        break;
                    }
                }
            }
            return result.toArray(new String[result.size()]);
        }catch (JSONException e){
            e.printStackTrace();
            return new String[1];
        }
    }
}

package com.ggfood.test.ggfood_api.fragment;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.Config;
import com.ggfood.test.ggfood_api.ExpandableListAdapter;
import com.ggfood.test.ggfood_api.R;
import com.ggfood.test.ggfood_api.STTService;
import com.ggfood.test.ggfood_api.Teaching;
import com.ggfood.test.ggfood_api.tool.Http;
import com.ggfood.test.ggfood_api.tool.WebImageView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Wei on 2017/3/20.
 */

public class Recommend extends BaseFragment  implements STTService.STTEvent {
    public static final String TAG = "Recommend";
    boolean startTeache = false;
    Button button;
    WebImageView webImageView;
    TextView recommend_name;
    ScrollView scrollView;
    public static int id;
    SQLiteDatabase sqLiteDatabase;
    //ExpandableListView
    private View expandViewHeader;
    private ExpandableListView listView;
    private ExpandableListAdapter listAdapter;
    private String groupName[] = {"描述","食材","步驟"};
    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHashMap;
    private String preference = null;
    int _method;
    String _id,_name ,_love,_image,_categories,_description;
    String data;
    JSONArray _ingredients,_steps;
    int talk=0;
    public static String SPECIFY_CATEGORY_ARG = "specify";
    Handler httpHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case 1:
                    try {

                        data = msg.getData().getString("html");
                        Log.e(TAG,data);
                        JSONObject jsonObject = new JSONObject(data);
                        id=Integer.parseInt(jsonObject.getString("id"));
                        _id = jsonObject.getString("id");
                        _name = jsonObject.getString("name");
                        _love = jsonObject.getString("love");
                        _image = jsonObject.getString("image");
                        _categories = jsonObject.getString("categories");
                        _description = jsonObject.getString("description");
                        _ingredients=jsonObject.getJSONArray("ingredients");
                        _steps=jsonObject.getJSONArray("steps");
                        _method=jsonObject.optInt("method");

                        Log.e("recommend-DEBUG",jsonObject.optString("debug",""));
                        Log.e("recommend","method : "+_method);
                        setExpandableListAdapter();
                        setExpandViewHeader(jsonObject.getString("name"),jsonObject.getString("image"));

                        String speech = "";
                        switch(_method){
                            case 0://隨機
                                speech = "我隨機挑了一道食譜給你看看";
                                break;
                            case 1://季節
                                speech = "最近我覺得你可以吃"+_name;
                                break;
                            case 2://偏好
                                speech = "根據你的偏好我覺得你可能會喜歡吃"+_name;
                                break;
                            case 3://分類
                                speech = "如果是"+_categories+"的話，我會推薦你吃"+_name;
                                break;
                        }
                        binder.startSpeak(speech);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
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
            binder.registerListener(Recommend.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG,"onServiceDisconnected");
            binder.unregisterListener(Recommend.this);
            binder = null;
        }
    };
    /************************************************/
    @Override
    protected void initView(View rootView) {
        button=(Button)rootView.findViewById(R.id.recommend_btn);
        recommend_name=(TextView)rootView.findViewById(R.id.recommend_name);
        webImageView=(WebImageView)rootView.findViewById(R.id.recommend_imageView);
        listView=(ExpandableListView)rootView.findViewById(R.id.expandableListView);
        expandViewHeader = createHeader();
        listView.addHeaderView(expandViewHeader);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecommend();
            }
        });
        Bundle arg = getArguments();
        preference = getActivity().getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE).getString("preference",null);
        if(arg != null)
            startRecommend(getArguments().getString(SPECIFY_CATEGORY_ARG));
        else
            startRecommend();
    }
    private void initData() throws JSONException {
        listDataHeader=new ArrayList<>();
        listHashMap=new HashMap<>();
        listDataHeader.addAll(Arrays.asList(groupName));
        List<String> description=new ArrayList<>();
        description.add(_description);
        List<String> ingredients=new ArrayList<>();
        for(int i=0;i<_ingredients.length();i++){
            ingredients.add(_ingredients.getJSONObject(i).getString("name")+" :  "+_ingredients.getJSONObject(i).getString("weight"));
        }
        List<String> steps=new ArrayList<>();
        for(int i=0;i<_steps.length();i++){
            steps.add(_steps.getJSONObject(i).getString("step")+".   "+_steps.getJSONObject(i).getString("text"));
        }
        listHashMap.put(listDataHeader.get(0),description);
        listHashMap.put(listDataHeader.get(1),ingredients);
        listHashMap.put(listDataHeader.get(2),steps);
    }
    private void setExpandableListAdapter(){
        try {
            initData();
            listAdapter=new ExpandableListAdapter(getActivity(),listDataHeader,listHashMap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        listView.setAdapter(listAdapter);
    }
    public void startRecommend(){
//        String args = getArguments().getString(SPECIFY_CATEGORY_ARG);
//        setArguments(null);
        String url = "http://"+ Config.HOSTURL+"/ggfood/recommend.php?";       //設定目標的網址
        if(preference != null)//偏好
            url += "categories="+preference;
//        if(args != null){
//            url += "?specify=" + args+"&";
//        }
        Http request = new Http(url,httpHandler);
        request.send();
    }
    public void startRecommend(String category){
        if(category == null) {
            startRecommend();
        }else {
            String url = "http://" + Config.HOSTURL + "/ggfood/recommend.php?specify=" + category+"&";       //設定目標的網址
            if(preference != null)//偏好
                url += "categories="+preference;
            Http request = new Http(url, httpHandler);
            request.send();
        }
    }

    public void startTeaching(){

        Intent intent =new Intent(getActivity(),Teaching.class);
        Calendar c = Calendar.getInstance();
        String dateformat = "yyyy-MM-dd";
        SimpleDateFormat df = new SimpleDateFormat(dateformat);
        String today = df.format(c.getTime());
        Log.e("SQLite","INSERT INTO history (`id`, `recipe_name`, `recipe_id`, `date`) VALUES (NULL, '"+_name+"',"+_id+", '"+today+"');");
        final String dbname="ggfood";
        final String history_table="history";
        sqLiteDatabase=getActivity().openOrCreateDatabase(dbname, Context.MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("INSERT INTO history (`id`, `recipe_name`, `recipe_id`, `date`) VALUES (NULL, '"+_name+"',"+_id+", '"+today+"');");
        intent.putExtra("recipeData",data);
        startActivity(intent);
    }
    @Override
    protected int getMainLayoutId() {
        return R.layout.fragment_recommend;
    }

    @Override
    public void onSTTResult(JSONObject result) {
        try {
            Log.e(TAG,result.getString("action"));
            switch(result.getString("action")){
                case "what":{
                    if(_description.trim().length()>0){
                        binder.startSpeak(_description);
                    }else{
                        binder.startSpeak("我也不知道。他沒寫阿");
                    }
                    break;
                }
                case "recommend_accept":{
                    if(!startTeache) {
                        startTeache = true;
                        startTeaching();
                    }
                    break;
                }
            }
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
        if(!getActivity().bindService(it ,mConnection ,Context.BIND_AUTO_CREATE))
            showToast("bind service is failed");
        Log.e(TAG,"onResume");
        startTeache = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mConnection);
    }

    private View createHeader(){
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View v = inflater.inflate(R.layout.preview_recipe_header,null);
        WebImageView img = (WebImageView)v.findViewById(R.id.recommend_imageView);

        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTeaching();
            }
        });
        return v;
    }
    private void setExpandViewHeader(String title,String url){
        TextView tv_title = (TextView)expandViewHeader.findViewById(R.id.recommend_name);
        WebImageView img = (WebImageView)expandViewHeader.findViewById(R.id.recommend_imageView);

        tv_title.setText(title);
        img.setImageURI(url);
    }
}

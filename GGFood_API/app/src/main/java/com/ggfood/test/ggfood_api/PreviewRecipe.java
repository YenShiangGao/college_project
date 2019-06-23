package com.ggfood.test.ggfood_api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

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

public class PreviewRecipe extends AppCompatActivity  implements STTService.STTEvent{
    public static final String TAG = "PreviewRecipe";
    SharedPreferences sharedPreferences;
    ExpandableListView recipe_list;
    public static String data;
    private static JSONObject jsonObject;
    //ExpandableListView
    private View expandViewHeader;
    private ExpandableListAdapter listAdapter;
    private String groupName[] = {"描述","食材","步驟"};
    private List<String> listDataHeader;
    private HashMap<String,List<String>> listHashMap;
    SQLiteDatabase sqLiteDatabase;
    String _id,_name ,_love,_image,_categories,_description;
    JSONArray _ingredients,_steps;
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
            binder.registerListener(PreviewRecipe.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG,"onServiceDisconnected");
            binder.unregisterListener(PreviewRecipe.this);
            binder = null;
        }
    };
    /************************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_recipe);
        getSupportActionBar().hide();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(getIntent().getStringExtra("name"));
        toolbar.setNavigationIcon(R.drawable.ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        init();
        startSearch(getIntent().getIntExtra("id",0));
        sharedPreferences = this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);

    }
    public void startTeaching(){
        Intent intent =new Intent(this,Teaching.class);
        Calendar c = Calendar.getInstance();
        String dateformat = "yyyy-MM-dd";
        SimpleDateFormat df = new SimpleDateFormat(dateformat);
        String today = df.format(c.getTime());
        Log.e("SQLite","INSERT INTO history (`id`, `recipe_name`, `recipe_id`, `date`) VALUES (NULL, '"+_name+"',"+_id+", '"+today+"');");
        final String dbname="ggfood";
        final String history_table="history";
        sqLiteDatabase=openOrCreateDatabase(dbname, Context.MODE_PRIVATE,null);
        sqLiteDatabase.execSQL("INSERT INTO history (`id`, `recipe_name`, `recipe_id`, `date`) VALUES (NULL, '"+_name+"',"+_id+", '"+today+"');");
        intent.putExtra("recipeData",data);
        startActivity(intent);
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
            listAdapter=new ExpandableListAdapter(getApplicationContext(),listDataHeader,listHashMap);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        recipe_list.setAdapter(listAdapter);
    }
    private void init(){
        recipe_list=(ExpandableListView)findViewById(R.id.recipe_list);
        expandViewHeader = createHeader();
        recipe_list.addHeaderView(expandViewHeader);
    }
    private void startSearch(final int id){
        String url = "http://"+Config.HOSTURL+"/ggfood/get_full_recipe.php?recipe=" + id;       //設定目標的網址
        Http request = new Http(url,
                new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        data = msg.getData().getString("html");  //拿到網頁傳回的資料
                        try {
                            jsonObject = new JSONObject(data);
                            _id = jsonObject.getString("id");
                            _name = jsonObject.getString("name");
                            _love = jsonObject.getString("love");
                            _image = jsonObject.getString("image");
                            _categories = jsonObject.getString("categories");
                            _description = jsonObject.getString("description");
                            _ingredients=jsonObject.getJSONArray("ingredients");
                            _steps=jsonObject.getJSONArray("steps");

                            initData();
                            setExpandableListAdapter();
                            setExpandViewHeader(_name,_image);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        request.send();
    }

    @Override
    public void onSTTResult(JSONObject result) {
        try {
        Log.e(TAG,result.getString("action").toString());
        switch(result.getString("action").toString()){
            case "what":{
                if(_description.trim().length()>0){
                    binder.startSpeak(_description);
                }else{
                    binder.startSpeak("我也不知道。他沒寫阿");
                }
                break;
            }
            case "recommend_accept":{
                startTeaching();
                break;
            }
            case "recommend":{
                finish();
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

        }} catch (JSONException e) {
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
    protected void onResume() {
        super.onResume();
        Intent it = new Intent(this,STTService.class);
        if(!bindService(it,mConnection, Context.BIND_AUTO_CREATE))
            showToast("bind service is failed");
        Log.e(TAG,"onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }
    private View createHeader(){
        LayoutInflater inflater = LayoutInflater.from(this);
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


package com.ggfood.test.ggfood_api;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.tool.Http;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Search extends AppCompatActivity implements STTService.STTEvent{
    static boolean isStart = false;
    Toast toast;
    public static final String TAG = "Search";
    public STTService.MyBinder binder = null;
    String data;
    List<Map<String, Object>> items;
    String arrayData[];
    SimpleAdapter simpleAdapter;
    SearchView searchView,searchView2;
    ListView listView;
    String recipe="",categories="",ingredient="";
    String search;
    RadioGroup radioGroup;
    int textWidth = 550;
    Paint paint;
    TextView simpleTextView;

    private boolean SP_mode = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        isStart = true;
        getSupportActionBar().hide();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("食譜搜尋");
        toolbar.setNavigationIcon(R.drawable.ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        listView=(ListView)findViewById(R.id.listview);
        items = new ArrayList<Map<String,Object>>();
        searchView=(SearchView) findViewById(R.id.searchView);
        searchView2=(SearchView) findViewById(R.id.searchview2);
        radioGroup=(RadioGroup)findViewById(R.id.search_radio_group);
        radioGroup.check(R.id.search_radio_btn);
        search = radioGroup.getCheckedRadioButtonId() == R.id.search_radio_btn? "name":"categories";
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                search = checkedId == R.id.search_radio_btn? "name":"categories";
            }
        });

        if(getIntent().getStringExtra("recipe")!=null){
            String recipe = getIntent().getStringExtra("recipe");
            StringBuilder sb = new StringBuilder(recipe.length());
            if(recipe.length() > 1) {
                sb.append(recipe, 1, recipe.length() - 1);
                searchView.setQuery(sb.toString(),true);
                startSearch(sb.toString(),"","");
                radioGroup.check(R.id.search_radio_btn);
                searchView2.setQuery("",true);
            }
        }else if(getIntent().getStringExtra("ingredient")!=null){
            String ingredient= getIntent().getStringExtra("ingredient");
            SP_mode = getIntent().getBooleanExtra("reading",false);
            StringBuilder sb = new StringBuilder(ingredient.length());
            if(ingredient.length() > 1) {
                sb.append(ingredient, 1, ingredient.length() - 1);
                searchView2.setQuery(sb.toString(),true);
                if(getIntent().getStringExtra("categories")!=null){
                    StringBuilder sb1 = new StringBuilder(ingredient.length());
                    if(ingredient.length() > 1) {
                        sb1.append(getIntent().getStringExtra("categories"), 1, getIntent().getStringExtra("categories").length() - 1);}
                    searchView.setQuery(sb1.toString(),true);
                    radioGroup.check(R.id.search_radio_btn2);
                    startSearch("",sb1.toString(),sb.toString());
                }else{
                    startSearch("","",sb.toString());
                    searchView.setQuery("",true);
                }

            }
        }
        searchView2.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SP_mode = false;
                checkquery();
                if(search.equals("name")){
                    startSearch(recipe,"",query);
                }else{
                    startSearch("",categories,query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ingredient=newText;
                return false;
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SP_mode = false;
                checkquery();
                if(search.equals("name")){
                        startSearch(query,"",ingredient);
                }else{
                    startSearch("",query,ingredient);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(search.equals("name")){
                    recipe=newText;
                }else{
                    categories=newText;
                }
                return false;
            }
        });
        //計算Text寬度用
        LinearLayout itemLayout = (LinearLayout)LayoutInflater.from(this).inflate(R.layout.listview_style,null);
        simpleTextView = (TextView)itemLayout.findViewById(R.id.textview_name);
        paint = simpleTextView.getPaint();
        Intent it = new Intent(this,STTService.class);
        if(!bindService(it,mConnection, Context.BIND_AUTO_CREATE))
            showToast("bind service is failed");
    }
    private void checkquery(){
        search = radioGroup.getCheckedRadioButtonId() == R.id.search_radio_btn? "name":"categories";
        if(search.equals("name")){
            recipe=searchView.getQuery().toString();
            categories="";
        }else{
            categories=searchView.getQuery().toString();
            recipe="";
        }
    }
    private void showToast(String msg){
        toast.setText(msg);
        toast.show();
    }
    private void startSearch(String recipe,String categories,String ingredients){
        searchView.setFocusable(false);
        searchView2.setFocusable(false);
        //清除先前資料
        items.clear();
        String url="http://"+Config.HOSTURL+"/ggfood/ggfoodSearch.php?recipe="+recipe+"&categories="+categories+"&ingredients="+ingredients;
       // String url="http://"+Config.HOSTURL+"/ggfood/query.php?recipe="+query+"&categories=";
        Http request = new Http(url);
        request.setHandler(new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Log.e("onQueryTextSubmit",msg.getData().getString("html"));
                data=msg.getData().getString("html");
                if(!data.equals("0")){
                    arrayData=data.split("<br><br>");
                    JSONObject obj;
                    for (int i=0;i<arrayData.length;i++){
                        try {
                            obj=new JSONObject(arrayData[i]);
                            Map<String, Object> item = new HashMap<String, Object>();
                            String small = obj.getString("image").replace("large","small");
                            item.put("webview", small);
                            item.put("textview", getShortString(obj.getString("name")));
                           // Log.e("webview","\n"+obj.getString("name")+"\n"+obj.getString("image"));
                            items.add(item);
                            //Log.e("data",data);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    simpleAdapter=new SimpleAdapter(Search.this,items,R.layout.listview_style,new String[]{"webview", "textview"},
                            new int[]{R.id.webview_image, R.id.textview_name});

                    listView.setAdapter(simpleAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Log.e("setOnItemClickListener","1212311313");
                            Intent intent = new Intent(Search.this,PreviewRecipe.class);
                            try {
                                JSONObject obj= new JSONObject(arrayData[position]);
                                intent.putExtra("id",obj.getInt("id"));
                                intent.putExtra("name",obj.getString("name"));
                                startActivity(intent);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                           // startSearchOne(position);
                        }
                    });
                }else{
                }
                //特殊模式
                if (binder != null && arrayData != null) {
                    if(SP_mode){
                        try {
                            JSONObject obj= new JSONObject(arrayData[0]);
                            binder.startSpeak("你說的是"+obj.getString("name")+"嗎");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }else {
                        binder.startSpeak("找到了" + arrayData.length + "筆相關的食譜");
                    }
                } else if (arrayData == null) {
                    binder.startSpeak("沒有找到");
                }
            }
        });
        request.send();
    }
    private int getTextWidth(String text){
        Rect bounds = new Rect();
        paint.getTextBounds(text,0,text.length(),bounds);
        return bounds.right - bounds.left;
    }
    private String getShortString(String text){
        Log.e("getShortStrig_START",text);
        int w = simpleTextView.getPaddingRight() + simpleTextView.getPaddingLeft();
        int width = getTextWidth(text);
        int dotWidth = getTextWidth("...");
        Log.e("getShortString",String.format("w = %d , width = %d",w,width));
        Log.e("getShortString"," : "+simpleTextView.getWidth());

        if(width + w + dotWidth > textWidth){
            StringBuilder builder = new StringBuilder(text);
            while(true){
                builder.deleteCharAt(builder.length()-1);
                width = getTextWidth(builder.toString());
                if(width + w + dotWidth < textWidth){
                    builder.append("...");
                    return builder.toString();
                }
                Log.e("getShortStrig",String.format("w = %d , width = %d",w,width));
            }
        }else{
            return text;
        }
    }

    /************************************************/
    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.e(TAG,"onServiceConnected");
            binder = ((STTService.MyBinder)service);
            binder.registerListener(Search.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG,"onServiceDisconnected");
            binder.unregisterListener(Search.this);
            binder = null;
        }
    };
    /************************************************/

    @Override
    public void onSTTResult(JSONObject result) {
        try {
            switch(result.getString("action")){
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

                case "search_recipe":{
                   finish();
//                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
//                    StringBuilder sb = new StringBuilder(params.get("recipe").toString().length());
//                    if(params.get("recipe").toString().length() > 1) {
//                        sb.append(params.get("recipe").toString(), 1, params.get("recipe").toString().length() - 1);
//                    }
//                    radioGroup.check(R.id.search_radio_btn);
//                    searchView.setQuery(sb.toString(),true);
//                    startSearch(sb.toString(),"","");
//                    searchView2.setQuery("",true);
                    break;
                }case "search_ingredient":{
                    finish();
//                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
//                    StringBuilder sb1 = null;
//                    if(params.get("pecies")!=null){
//                        sb1 = new StringBuilder(params.get("pecies").toString().length());
//                        if(params.get("pecies").toString().length() > 1) {
//                            sb1.append(params.get("pecies").toString(), 1, params.get("pecies").toString().length() - 1);
//                        }
//                    }
//                    StringBuilder sb2 = new StringBuilder(params.get("ingredient").toString().length());
//                    if(params.get("ingredient").toString().length() > 1) {
//                        sb2.append(params.get("ingredient").toString(), 1, params.get("ingredient").toString().length() - 1);
//                    }
//                    searchView2.setQuery(sb2.toString(),true);
//                    if(sb1!=null){
//                        searchView.setQuery(sb1.toString(),true);
//                        radioGroup.check(R.id.search_radio_btn2);
//
//                        startSearch("",sb1.toString(),sb2.toString());
//                    }else{
//                        startSearch("","",sb2.toString());
//                        searchView.setQuery("",true);
//                    }

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
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
        isStart = false;
    }
}

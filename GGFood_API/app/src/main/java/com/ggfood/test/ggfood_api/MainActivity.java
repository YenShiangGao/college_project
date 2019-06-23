package com.ggfood.test.ggfood_api;
import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.fragment.History;
import com.ggfood.test.ggfood_api.fragment.HomePage;
import com.ggfood.test.ggfood_api.fragment.IceBox;
import com.ggfood.test.ggfood_api.fragment.Order;
import com.ggfood.test.ggfood_api.fragment.Personal;
import com.ggfood.test.ggfood_api.fragment.Recommend;
import com.ggfood.test.ggfood_api.fragment.Settings;
import com.ggfood.test.ggfood_api.tool.AsyncHttpSender;
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

import ai.api.model.AIOutputContext;

public class MainActivity extends AppCompatActivity implements STTService.STTEvent {
    public static final String TAG = "MainActivity";//TAG
    Button menuButton, loginButton; //選單按鈕 登入按鈕
    SharedPreferences sharedPreferences;//存取資料
    SQLiteDatabase sqLiteDatabase;//手機資料庫
    TextView nick;//使用者登入後顯示暱稱
    ImageView imageView;//首頁圖片
    ImageButton search;//搜尋按鈕
    DrawerLayout drawerLayout;//左側選單
    NavigationView nav_view;//左側選單內容
    //Fragment
    Fragment currentFragement;
    FragmentTransaction transaction;
    FragmentManager fragmentManager;
    public STTService.MyBinder binder = null;//STT Service
    Toast toast;//訊息提示

    //Fragment 換頁
    public void changeFragement(Class<? extends Fragment> fragmentClass, Bundle args) {
        try {
            currentFragement = fragmentClass.newInstance();
            currentFragement.setArguments(args);
            transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.content_frame, currentFragement, currentFragement.getClass().getSimpleName());
            Log.d("changeFragment", "to " + currentFragement.getClass().getSimpleName());
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void changeFragement(Class<? extends Fragment> fragmentClass) {
        changeFragement(fragmentClass, null);
    }

    private void initFragement() {
        fragmentManager = getFragmentManager();
        try {
            currentFragement = new HomePage();
            transaction = fragmentManager.beginTransaction();
            transaction.add(R.id.content_frame, currentFragement, currentFragement.getClass().getSimpleName());
            Log.d("changeFragment", "to " + currentFragement.getClass().getSimpleName());
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkLogin() {
        if (sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF, false)) {
            Map<String, ?> stringMap = sharedPreferences.getAll();
            loginButton.setText("登出");
            nick.setText(stringMap.get("nick").toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setImageDrawable(getDrawable(R.drawable.robot));
            }

        } else {
            nick.setText("");
            loginButton.setText("登入");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                imageView.setImageDrawable(getDrawable(R.drawable.robot2));
            }
        }


    }//檢查是否登入

    private void createDatabase() {
        final String dbname = "ggfood";
        final String history_table = "history";
        sqLiteDatabase = openOrCreateDatabase(dbname, Context.MODE_PRIVATE, null);
        String createTable = "CREATE TABLE IF NOT EXISTS " + history_table + " (id INT AUTO_INCREMENT,recipe_name VARCHAR(100),recipe_id INT,date Date,PRIMARY KEY (id));";
        sqLiteDatabase.execSQL(createTable);
    }//建立手機資料庫

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            Log.e(TAG, "onServiceConnected");
            binder = ((STTService.MyBinder) service);
            binder.registerListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            Log.e(TAG, "onServiceDisconnected");
            binder.unregisterListener(MainActivity.this);
            binder = null;
        }
    };//連接Service
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide(); //隱藏標題
        initFragement();//初始化Fragement
        createDatabase();//建立資料庫，已建立忽略
        setConfig();//取得設定資料
        sharedPreferences = this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);//設定存取資料模式
        // 權限
        checkAudioRecordPermission();
        toast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        //初始化 action bar
        menuButton = (Button) findViewById(R.id.button);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(Gravity.LEFT);
            }
        });
        //防止Icon被填入灰色
        nav_view = (NavigationView) findViewById(R.id.nav_view);
        nav_view.setItemIconTintList(null);
        nav_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {//設定左邊欄內容
                int id = item.getItemId();
                switch (id) {
                    case R.id.nav_home: {
                        Log.e("Item", "home");
                        changeFragement(HomePage.class);
                        drawerLayout.closeDrawers();
                        break;
                    }
                    case R.id.nav_doc: {
                        Log.e("Item", "doc");
                        changeFragement(Personal.class);
                        drawerLayout.closeDrawers();
                        break;
                    }
                    case R.id.nav_good: {
                        Log.e("Item", "good");
                        changeFragement(Recommend.class);
                        drawerLayout.closeDrawers();
                        break;
                    }
                    case R.id.nav_fredge: {
                        Log.e("Item", "fredge");
                        changeFragement(IceBox.class);
                        drawerLayout.closeDrawers();
                        break;
                    }
                    case R.id.nav_history: {
                        Log.e("Item", "history");
                        changeFragement(History.class);
                        drawerLayout.closeDrawers();
                        break;
                    }
                    case R.id.nav_settings: {
                        Log.e("Item", "settings");
                        changeFragement(Settings.class);
                        drawerLayout.closeDrawers();
                        break;
                    }
                    case R.id.nav_order: {
                        Log.e("Item", "order");
                        changeFragement(Order.class);
                        drawerLayout.closeDrawers();
                        break;
                    }
                }
                return false;
            }
        });
        //初始化側邊選單、Fragment、action bar和各個介面上的按鈕
        //連接STT Service
        Intent it = new Intent(this, STTService.class);
        if (!bindService(it, mConnection, Context.BIND_AUTO_CREATE))
            showToast("bind service is failed");
        //login
        loginButton = (Button) nav_view.getHeaderView(0).findViewById(R.id.btn_login);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF, false)) {
                    sharedPreferences.edit().clear().commit();
                    Toast.makeText(MainActivity.this, "已登出", Toast.LENGTH_SHORT).show();
                    checkLogin();
                } else {
                    Intent intent = new Intent(MainActivity.this, Login.class);
                    startActivity(intent);
                }
            }
        });
        imageView = (ImageView) nav_view.getHeaderView(0).findViewById(R.id.login_image);//設定登入圖片
        nick = (TextView) nav_view.getHeaderView(0).findViewById(R.id.nick_tv);//設定暱稱
        //search 按鈕設定
        search = (ImageButton) findViewById(R.id.search_btn);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Search.class);
                startActivity(intent);
            }
        });
        checkLogin();//檢查登入
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkLogin();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mConnection);
    }

    @Override
    public void onSTTResult(JSONObject result) {
        Log.e("onSTTResult", result.toString());
        try {
            switch (result.getString("action")) {
                case "recommend": {//一般推薦
                    if (currentFragement instanceof Recommend) {
                        ((Recommend) currentFragement).startRecommend();
                    } else {
                        drawerLayout.closeDrawers();
                        changeFragement(Recommend.class);
                    }
                    break;
                }
                case "recommend_catalog": {//指定分類的推薦
                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
                    if (currentFragement instanceof Recommend) {
                        ((Recommend) currentFragement).startRecommend(params.get("pecies").getAsString());
                    } else {
                        drawerLayout.closeDrawers();
                        Bundle args = new Bundle();
                        args.putString(Recommend.SPECIFY_CATEGORY_ARG, params.get("pecies").getAsString());
                        changeFragement(Recommend.class, args);
                    }
                    break;
                }
                case "search_recipe": {
                    Intent intent = new Intent(MainActivity.this, Search.class);
                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
                    intent.putExtra("recipe", params.get("recipe").toString());
                    startActivity(intent);

                    break;
                }
                case "search_ingredient": {

                    Intent intent = new Intent(MainActivity.this, Search.class);
                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
                    intent.putExtra("ingredient", params.get("ingredient").toString());
                    if (params.get("pecies") != null)
                        intent.putExtra("categories", params.get("pecies").toString());
                    startActivity(intent);

                    break;
                }
                case "search_ingredient_plus": {
                    Intent intent = new Intent(MainActivity.this, Search.class);
                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
                    intent.putExtra("ingredient", params.get("ingredient").toString());
                    if (params.get("pecies") != null)
                        intent.putExtra("categories", params.get("pecies").toString());
                    intent.putExtra("reading", true);//特殊模式
                    startActivity(intent);
                    break;
                }
                case "warehouse": {
                    List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
                    Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM ice_box", null);
                    if (cursor.getCount() != 0) {
                        cursor.moveToFirst();
                        do {
                            Map<String, Object> map = new HashMap<>();
                            map.put("NAME", cursor.getString(1));
                            map.put("QUANTITY", cursor.getString(2));
                            mapList.add(map);
                            Log.e("冰箱", cursor.getString(1) + cursor.getString(2));
                        } while (cursor.moveToNext());
                        String str = "冰箱裡有";
                        for (int i = 0; i < mapList.size(); i++) {
                            str += mapList.get(i).get("NAME") + "。" + mapList.get(i).get("QUANTITY") + "。";
                        }
                        binder.startSpeak(str);
                    } else {
                        binder.startSpeak("冰箱裡沒有東西");
                    }

                    break;
                }
                case "icebox_what": {
                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
                    List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
                    Cursor cursor = sqLiteDatabase.rawQuery("SELECT * FROM ice_box", null);
                    if (cursor.getCount() != 0) {
                        cursor.moveToFirst();
                        do {
                            Map<String, Object> map = new HashMap<>();
                            map.put("NAME", cursor.getString(1));
                            map.put("QUANTITY", cursor.getString(2));
                            mapList.add(map);
                        } while (cursor.moveToNext());
                    }
                    boolean yesorno = false;
                    for (int i = 0; i < mapList.size(); i++) {
                        Log.e("iceBOX", mapList.get(i).get("NAME").toString() + "  :  " + params.get("any").toString());
                        String recipe = params.get("any").toString();
                        StringBuilder sb = new StringBuilder(recipe.length());
                        if (recipe.length() > 1) {
                            sb.append(recipe, 1, recipe.length() - 1);
                        }
                        if (mapList.get(i).get("NAME").toString().equals(sb.toString())) {

                            binder.startSpeak("有");
                            yesorno = true;
                            break;
                        }
                    }
                    if (!yesorno) {

                        String url = "http://" + Config.HOSTURL + "/ggfood/shop/query/product_search.php";
                        try {
                            AsyncHttpSender.Request request = new AsyncHttpSender.Request(url);
                            request.setPostData("name", params.get("any").getAsString());
                            AsyncHttpSender.send(request,
                                    new AsyncHttpSender.ResponseListener() {
                                        @Override
                                        public Object onResponse(int responseCode, Map<String, List<String>> headers, InputStream is) {
                                            try {
                                                BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                                                String response;
                                                StringBuilder sb = new StringBuilder(20);
                                                String v;
                                                while ((v = in.readLine()) != null) {
                                                    sb.append(v);
                                                }
                                                response = sb.toString();
                                                Log.e("onResponse", response);
                                                try {
                                                    JSONObject jsonobject = new JSONObject(response);
                                                    JSONArray jsonarray = jsonobject.getJSONArray("results");
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
                                            if (data != null) {
                                                try {
                                                    JSONArray arr = (JSONArray) data;
                                                    JSONObject item = arr.getJSONObject(0);
                                                    if (arr.length() >= 1) {
                                                        binder.startSpeak("沒有。要不要訂購呢。現在" + item.getString("name") + "每份" + item.getString("unit") + item.getString("price") + "元");
                                                        binder.textRequest("order set context " + item.getInt("id"));
                                                    } else {
                                                        binder.startSpeak("沒有");
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                    binder.startSpeak("沒有");
                                                }
                                            }
                                        }
                                    });
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                case "order_quantity": {
                    HashMap<String, JsonElement> params = (HashMap<String, JsonElement>) result.get("Parameters");
                    List<AIOutputContext> contexts = (List<AIOutputContext>) result.get("contexts");
                    for (AIOutputContext context : contexts) {
                        if (context.getName().equals("order_info")) {
                            //數量
                            try {
                                int quantity = params.get("quantity").getAsInt();
                                int id = context.getParameters().get("id").getAsInt();
                                Log.e("order_quantity", context.getParameters().toString());
                                Log.e("order_quantity", "id : " + id + "  quantity : " + quantity);
                                sendOrderRequest(id, quantity);
                                break;
                            } catch (Exception e) {
                                Toast.makeText(this, "不明的錯誤", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                    break;
                }
                case "open_history": {
                    Log.e("Item", "history");
                    changeFragement(History.class);
                    drawerLayout.closeDrawers();
                    break;
                }
                case "open_scan_activity": {
                    currentFragement = new IceBox();
                    Bundle bundle = new Bundle();
                    bundle.putBoolean("auto_start_qr_scan", true);
                    currentFragement.setArguments(bundle);
                    transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.content_frame, currentFragement, currentFragement.getClass().getSimpleName());
                    Log.d("changeFragment", "to " + currentFragement.getClass().getSimpleName());
                    transaction.commitAllowingStateLoss();
                }
                break;
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }//STT Service 傳回結果

    @Override
    public void onStartRecord() {
        showToast("START");
    }//開始聆聽 提示

    @Override
    public void onStopRecord() {
        showToast("STOP");
    }//結束聆聽 提示

    private void showToast(String msg) {
        toast.setText(msg);
        toast.show();
    }//提示

    protected void checkAudioRecordPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.RECORD_AUDIO},
                        33);
            }
        }
    }//檢查權限

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }//權限

    private void sendOrderRequest(int p_id, int quantity) {
        Log.e("sendOrderRequest", "run" + sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF, false));
        if (sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF, false)) {
            String m_id = "";
            String address = "";
            String phone = "";
            String url = "";
            try {
                m_id = sharedPreferences.getString("account_id", "");
                address = sharedPreferences.getString("address", "");
                phone = sharedPreferences.getString("phone", "");
                url = "http://" + Config.HOSTURL + "/ggfood/shop/query/order_insert.php?id=" + m_id;
                Log.e("sendOrderRequest",
                        "測試" +
                                address + "\n" +
                                phone + "\n" +
                                url);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                AsyncHttpSender.Request request = new AsyncHttpSender.Request(url);
                request.setPostData("address", address, "payment_method", "1", "delivery_method", "0", "phone", phone, "note", "系統訂購", "pid[]", "" + p_id, "quantity[]", "" + quantity);
                AsyncHttpSender.send(request,
                        new AsyncHttpSender.ResponseListener() {
                            @Override
                            public Object onResponse(int responseCode, Map<String, List<String>> headers, InputStream is) {
                                try {
                                    BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                                    String response;
                                    StringBuilder sb = new StringBuilder(20);
                                    String v;
                                    while ((v = in.readLine()) != null) {
                                        sb.append(v);
                                    }
                                    response = sb.toString();
                                    Log.e("onResponse", response);
                                    JSONObject jsonObject = new JSONObject(response);
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
                                if (data != null)
                                    Toast.makeText(MainActivity.this, data.toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "請先登入", Toast.LENGTH_SHORT).show();
        }


    }//傳送訂單要求

    private void setConfig() {
        SharedPreferences settings = this.getSharedPreferences("Config", 0);
        Config.HOSTURL = settings.getString("HOSTURL", "wei18963.ddns.net");
        Config.pitch = settings.getFloat("PITCH", 1.3f);
        Config.rate = settings.getFloat("RATE", 1.5f);
    }//取得設定資料
}



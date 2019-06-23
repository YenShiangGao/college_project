package com.ggfood.test.ggfood_api;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.tool.AsyncHttpSender;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

public class Login extends AppCompatActivity {
    Button login_btn,register_btn;
    EditText acc_ed,pas_ed;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("會員登入");
//        getDrawable(android.R.attr.actionModeCloseDrawable);
        toolbar.setNavigationIcon(R.drawable.ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        acc_ed=(EditText)findViewById(R.id.login_edittext_account);
        pas_ed=(EditText)findViewById(R.id.login_edittext_password);
        //登入
        login_btn=(Button)findViewById(R.id.login_btn);
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                String login_url="http://"+Config.HOSTURL+"/ggfood/login.php";
//                final String account=acc_ed.getText().toString().trim();
//                final String password=pas_ed.getText().toString().trim();
//                StringRequest stringRequest=new StringRequest(Request.Method.POST, login_url, new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        try {
//                            JSONObject jsonObject =new JSONObject(response);
//                            if(jsonObject.getString("code").equals("0")){
//                                SharedPreferences sharedPreferences = Login.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
//                                SharedPreferences.Editor editor = sharedPreferences.edit();
//                                editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, true);
//                                editor.putString("account_id", jsonObject.getString("id"));
//                                editor.putString("account",  jsonObject.getString("account"));
//                                editor.putString("password",  jsonObject.getString("password"));
//                                editor.putString("nick",  jsonObject.getString("nick"));
//                                editor.putString("email",  jsonObject.getString("email"));
//                                editor.putString("sex",  jsonObject.getString("sex"));
//                                editor.putString("address",  jsonObject.getString("address"));
//                                editor.putString("phone",  jsonObject.getString("phone"));
//                                editor.commit();
//                                Toast.makeText(Login.this,jsonObject.getString("msg"),Toast.LENGTH_SHORT).show();
//                                Log.e("loginsuccess",response);
//                                finish();
//                            }else{
//                                Log.e("loginerror",jsonObject.getString("code"));
//                                Toast.makeText(Login.this,jsonObject.getString("msg"),Toast.LENGTH_SHORT).show();
//                            }
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }, new Response.ErrorListener() {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//
//                    }
//                }){
//                    @Override
//                    protected Map<String, String> getParams() throws AuthFailureError {
//                        Map<String,String> params = new HashMap<>();
//                        //Adding parameters to request
//                        params.put("acc", account);
//                        params.put("pas", password);
//                        //returning parameter
//                        return params;
//                    }
//                };
//                RequestQueue requestQueue = Volley.newRequestQueue(Login.this);
//                requestQueue.add(stringRequest);

                String login_url="http://"+Config.HOSTURL+"/ggfood/login.php";
                final String account=acc_ed.getText().toString().trim();
                final String password=pas_ed.getText().toString().trim();
                try {
                    AsyncHttpSender.Request request = new AsyncHttpSender.Request(login_url);
                    request.setPostData("acc",account,"pas",password);
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
                                if(jsonObject.getString("code").equals("0")){
                                    JSONObject obj = jsonObject.getJSONObject("info");
                                    //保存登入後取得的資料
                                    SharedPreferences sharedPreferences = Login.this.getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putBoolean(Config.LOGGEDIN_SHARED_PREF, true);
                                    editor.putString("account_id", obj.getString("id"));
                                    editor.putString("account",  obj.getString("account"));
                                    editor.putString("password",  obj.getString("password"));
                                    editor.putString("nick",  obj.getString("nick"));
                                    editor.putString("email",  obj.getString("email"));
                                    editor.putString("sex",  obj.getString("sex"));
                                    editor.putString("address",  obj.getString("address"));
                                    editor.putString("phone",  obj.getString("phone"));
                                    editor.commit();

                                    Log.e("loginsuccess",response);
                                    finish();
                                }else{
                                    Log.e("loginerror",jsonObject.getString("code"));
                                }
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
                                Toast.makeText(Login.this,data.toString(),Toast.LENGTH_SHORT).show();
                        }
                    });
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        //註冊
        register_btn=(Button)findViewById(R.id.register_btn);
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this,Register.class);
                startActivity(intent);
            }
        });
    }
}

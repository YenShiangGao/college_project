package com.ggfood.test.ggfood_api;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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

public class Register extends AppCompatActivity {
    EditText account_ed,password_ed,password_check_ed,nick_ed,address_ed,email_ed,phone_ed;
    Button register_btn;
    String sex;
    RadioGroup radioGroup;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_register);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("註冊");
        toolbar.setNavigationIcon(R.drawable.ic_ab_back_material);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        account_ed=(EditText)findViewById(R.id.account_ed);
        password_ed=(EditText)findViewById(R.id.password_ed);
        password_check_ed=(EditText)findViewById(R.id.password_check_ed);
        nick_ed=(EditText)findViewById(R.id.nick_ed);
        account_ed=(EditText)findViewById(R.id.account_ed);
        address_ed=(EditText)findViewById(R.id.address_ed);
        email_ed=(EditText)findViewById(R.id.email_ed);
        phone_ed=(EditText)findViewById(R.id.phone_ed);
        radioGroup=(RadioGroup)findViewById(R.id.radio_sex);
        register_btn=(Button)findViewById(R.id.register_btn);
        register_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password_ed.getText().toString().equals(password_check_ed.getText().toString())){
                    String login_url="http://"+Config.HOSTURL+"/ggfood/register.php";
                    final String account=account_ed.getText().toString().trim();
                    final String password=password_ed.getText().toString().trim();
                    final String nick=nick_ed.getText().toString().trim();
                    final String address=address_ed.getText().toString().trim();
                    final String email=email_ed.getText().toString().trim();
                    final String phone=phone_ed.getText().toString().trim();
                    final String sex = radioGroup.getCheckedRadioButtonId() == R.id.radioButton2? "1":"0";
//                    StringRequest stringRequest=new StringRequest(Request.Method.POST, login_url, new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        Log.e("Register",response);
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
//                        params.put("nick", nick);
//                        params.put("address", address);
//                        params.put("email", email);
//                        params.put("phone", phone);
//                        params.put("sex", sex);
//
//                        //returning parameter
//                        return params;
//                    }
//                };
//                RequestQueue requestQueue = Volley.newRequestQueue(Register.this);
//                requestQueue.add(stringRequest);

                    try {
                        AsyncHttpSender.Request request = new AsyncHttpSender.Request(login_url);
                        request.setPostData("acc",account,"pas",password,"nick",nick,"address",address,"email",email,"phone",phone,"sex",sex);
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
                                            Toast.makeText(Register.this,data.toString(),Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }catch (IOException e){
                        e.printStackTrace();
                    }
                }else{
                    Toast.makeText(Register.this,"密碼與前面输入的字符串不相符。請在試一次",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}

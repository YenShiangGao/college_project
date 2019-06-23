package com.ggfood.test.ggfood_api.fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.Config;
import com.ggfood.test.ggfood_api.R;
import com.ggfood.test.ggfood_api.tool.AsyncHttpSender;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Created by Wei on 2017/3/20.
 */

public class Personal extends BaseFragment {
    TextView account,preference_2;
    EditText nick,phone,address,email;
    String account_id;
    Button save;
    SharedPreferences sharedPreferences;
    String[] preferenceOption = {"派塔、泡芙","果凍、茶凍","炸物","涼拌","沙拉","披薩","滷肉","麻辣","烤肉","滷味","咖哩","焗烤","肉丸子","熱炒","鍋物","素食","湯","麵食","米食","甜湯","鬆餅","奶酪","布丁","優格","麻糬","北方餅","日式點心","巧克力","手工餅乾","泡芙","中式點心","麵包吐司","蛋糕","韓式","日式","泰式","西班牙","法式","港式","義式","台灣小吃","眷村菜","客家","雞肉","豬肉","羊肉","鴨肉","牛肉","豆類","蛋","五穀雜糧","水果","菌藻","葉菜","海鮮","根莖","瓜果","冰","飲料","兒童餐","寶寶副食品","早餐","早午餐","下午茶","宵夜","便當","野餐","快速","省錢","果醬抹醬","餡料","醬料","微波爐","悶燒杯","烤箱","麵包機","鑄鐵鍋","電鍋","低卡瘦身","養顏美容","滋補養身","年菜","元宵節","萬聖節","聖誕","冬至湯圓","端午包粽","中式"};
    ArrayList<Integer> mSelectedPreferenceTmp = new ArrayList<>();
    ArrayList<Integer> mSelectedPreference = new ArrayList<>();
    @Override
    protected void initView(View rootView) {
        account = (TextView)rootView.findViewById(R.id.account);
        nick = (EditText)rootView.findViewById(R.id.nick);
        phone  = (EditText)rootView.findViewById(R.id.phone);
        address  = (EditText)rootView.findViewById(R.id.address);
        email  = (EditText)rootView.findViewById(R.id.email);
        preference_2=(TextView)rootView.findViewById(R.id.preference_2);
        Arrays.sort(preferenceOption);
        preference_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createDialog().show();
            }
        });
        save = (Button)rootView.findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String login_url="http://"+Config.HOSTURL+"/ggfood/update_member.php";
                final String _account = account.getText().toString().trim();
                final String _nick =nick.getText().toString().trim();
                final String _phone =phone.getText().toString().trim();
                final String _email =email.getText().toString().trim();
                final String _address =address.getText().toString().trim();
                try {
                    AsyncHttpSender.Request request = new AsyncHttpSender.Request(login_url);
                    request.setPostData("nick",_nick,"address",_address,"email",_email,"phone",_phone);
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
                                            JSONObject jsonObject =new JSONObject(response);
                                            return jsonObject.getString("msg");


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
                                    if(data != null)
                                        Toast.makeText(getActivity(),data.toString(),Toast.LENGTH_SHORT).show();
                                }
                            });
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        });
        sharedPreferences = getActivity().getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF,false)){
            Map<String,?> stringMap = sharedPreferences.getAll();
            account.setText(stringMap.get("account").toString());
            nick.setText(stringMap.get("nick").toString());
            phone.setText(stringMap.get("phone").toString());
            address.setText(stringMap.get("address").toString());
            email.setText(stringMap.get("email").toString());
            account_id=stringMap.get("account_id").toString();

            String getPreferencr_url="http://"+Config.HOSTURL+"/ggfood/getPreference.php?id="+account_id;

            try {
                AsyncHttpSender.Request request = new AsyncHttpSender.Request(getPreferencr_url);
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
                                    Log.e("getPreferencr",response);
                                    String preferenceResult[]=response.split(",");
                                    sharedPreferences.edit().putString("preference",response).commit();
                                    String setPreference="";
                                    for(int i=0;i<preferenceResult.length;i++){
                                        setPreference+=preferenceResult[i]+"\n";
                                        int n = Arrays.binarySearch(preferenceOption,preferenceResult[i]);
                                        if(n != -1)
                                            mSelectedPreference.add(n);
                                    }

                                    return  setPreference;
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                            @Override
                            public void onFinish(int responseCode, Map<String, List<String>> headers, Object data) {
                                if(data != null)
                                preference_2.setText(data.toString());
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }

        }else{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Toast.makeText(getContext(),"請先登入",Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    protected int getMainLayoutId() {
        return R.layout.fragment_personal;
    }


    public Dialog createDialog() {
        mSelectedPreferenceTmp.addAll(mSelectedPreference);
        boolean[] isChecked = new boolean[preferenceOption.length];
        for(int i=0;i<mSelectedPreferenceTmp.size();i++){
            isChecked[mSelectedPreferenceTmp.get(i)] = true;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Set the dialog title

        builder.setTitle("偏好選單")
                // Specify the list array, the items to be selected by default (null for none),
                // and the listener through which to receive callbacks when items are selected
                .setMultiChoiceItems(preferenceOption, isChecked,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which,
                                                boolean isChecked) {
                                if (isChecked) {
                                    // If the user checked the item, add it to the selected items
                                    mSelectedPreferenceTmp.add(which);
                                } else if (mSelectedPreferenceTmp.contains(which)) {
                                    // Else, if the item is already in the array, remove it
                                    mSelectedPreferenceTmp.remove(Integer.valueOf(which));
                                }
                            }
                        })
                // Set the action buttons
                .setPositiveButton("確認", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK, so save the mSelectedItems results somewhere
                        // or return them to the component that opened the dialog
                        mSelectedPreference.clear();
                        mSelectedPreference.addAll(mSelectedPreferenceTmp);
                        mSelectedPreferenceTmp.clear();
                        String login_url="http://"+Config.HOSTURL+"/ggfood/setPreference.php?id="+account_id+"&";
                        for(int i=0;i<mSelectedPreference.size();i++){
                            login_url+="preference[]="+preferenceOption[mSelectedPreference.get(i)];
                            if(i!=mSelectedPreference.size()-1){
                                login_url+="&";
                            }
                        }
                        try {
                            AsyncHttpSender.Request request = new AsyncHttpSender.Request(login_url);
                            Log.e("addPreference",login_url);
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
                                            } catch (IOException e) {
                                                e.printStackTrace();
                                            }
                                            return null;
                                        }
                                        @Override
                                        public void onFinish(int responseCode, Map<String, List<String>> headers, Object data) {
                                            if(data != null)
                                                Toast.makeText(getActivity(),data.toString(),Toast.LENGTH_SHORT).show();
                                            updatePreference();
                                        }
                                    });
                        }catch (IOException e){
                            e.printStackTrace();
                        }



                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mSelectedPreferenceTmp.clear();
                    }
                });

        return builder.create();
    }
    public void updatePreference(){

        String setPreference="";
        for(int i=0;i<mSelectedPreference.size();i++){
            setPreference+=preferenceOption[mSelectedPreference.get(i)]+"\n";
        }
        preference_2.setText(setPreference);
    }
}

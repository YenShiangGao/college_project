package com.ggfood.test.ggfood_api.fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.Config;
import com.ggfood.test.ggfood_api.ShowOrder;
import com.ggfood.test.ggfood_api.R;
import com.ggfood.test.ggfood_api.tool.AsyncHttpSender;

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

/**
 * A simple {@link Fragment} subclass.
 */
public class Order extends BaseFragment {
    ListView listView;
    List<Map<String, Object>> items;
    SimpleAdapter simpleAdapter;
    SharedPreferences sharedPreferences;
    @Override
    protected void initView(View rootView) {
        listView=(ListView)rootView.findViewById(R.id.order_list);
        items = new ArrayList<Map<String,Object>>();
        sharedPreferences = getActivity().getSharedPreferences(Config.SHARED_PREF_NAME, Context.MODE_PRIVATE);
        if(sharedPreferences.getBoolean(Config.LOGGEDIN_SHARED_PREF,false)){
            Map<String,?> stringMap = sharedPreferences.getAll();
            String account_id=stringMap.get("account_id").toString();
            final String order_search_url="http://"+ Config.HOSTURL+"/ggfood/shop/query/order_search.php?id="+account_id;
            Log.e("order_search",order_search_url);
            try {
                AsyncHttpSender.Request request = new AsyncHttpSender.Request(order_search_url);
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
                                    Log.e("order_search",response);
                                    try {
                                        JSONObject jsonObject=new JSONObject(response.toString());
                                        if(jsonObject.getString("code").equals("0")){
                                            JSONArray jsonArray = jsonObject.getJSONArray("result");
                                            return jsonArray;
                                        }else{
                                            Log.e("order_search_error","code:"+jsonObject.getString("code"));
                                        }
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
                                if(data != null){
                                     final JSONArray jsonArray = (JSONArray)data;
                                    items.clear();

                                    for(int i=0;i<jsonArray.length();i++){
                                        try {
                                            Map<String,Object> item =new HashMap<>();
                                            JSONObject obj=new JSONObject(jsonArray.get(i).toString());
                                            item.put("order_id",obj.getString("id"));
                                            item.put("order_date",obj.getString("date"));
                                            switch (obj.getString("state")){
                                                case "0":{
                                                    item.put("order_state","出貨中");
                                                    break;
                                                }
                                                case "1":{
                                                    item.put("order_state","已前往物流中心");
                                                    break;
                                                }
                                                case "2":{
                                                    item.put("order_state","已送達取貨門市");
                                                    break;
                                                }
                                                case "3":{
                                                    item.put("order_state","買家自取");
                                                    break;
                                                }
                                            }
                                            items.add(item);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    simpleAdapter=new SimpleAdapter(getActivity(),items,R.layout.order_listview_style,new String[]{"order_id", "order_date","order_state"},
                                            new int[]{R.id.order_id, R.id.order_date,R.id.order_state});
                                    listView.addHeaderView(View.inflate(getActivity(),R.layout.order_list_haeder_style,null));
                                    listView.setAdapter(simpleAdapter);
                                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                            Intent intent =new Intent(getActivity(), ShowOrder.class);
                                            if(position!=0){
                                                try {
                                                    intent.putExtra("order",jsonArray.get(position-1).toString());
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                startActivity(intent);
                                            }
                                        }
                                    });
                                    //String msg = "";
                                    //Toast.makeText(getActivity(),msg,Toast.LENGTH_SHORT).show();
                                }

                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }


        }else{
                Toast.makeText(getActivity(),"請先登入",Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected int getMainLayoutId() {
        return R.layout.fragment_order;
    }
}

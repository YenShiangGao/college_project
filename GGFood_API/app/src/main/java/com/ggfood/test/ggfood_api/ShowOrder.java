package com.ggfood.test.ggfood_api;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShowOrder extends AppCompatActivity {
    final String TAG ="ShowOrder";
    List<Map<String,Object>> items;
    TextView id,date,state,address,total;
    SimpleAdapter simpleAdapter;
    ListView listview;
    int totalprice=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_order);
        id=(TextView)findViewById(R.id.order_number);
        date=(TextView)findViewById(R.id.order_date);
        state=(TextView)findViewById(R.id.order_state);
        address=(TextView)findViewById(R.id.order_address);
        listview=(ListView)findViewById(R.id.preview_list);
        total= (TextView) findViewById(R.id.order_total);
        items=new ArrayList<>();
        try {
            JSONObject jsonObject=new JSONObject(getIntent().getStringExtra("order").toString());
            id.setText(jsonObject.getString("id"));
            date.setText(jsonObject.getString("date"));
            switch (jsonObject.getString("state")){
                case "0":{
                    state.setText("出貨中");
                    break;
                }
                case "1":{
                    state.setText("已前往物流中心");
                    break;
                }
                case "2":{
                    state.setText("已送達取貨門市");
                    break;
                }
                case "3":{
                    state.setText("買家自取");
                    break;
                }
            }
            address.setText(jsonObject.getString("address"));
            JSONArray jsonArray=jsonObject.getJSONArray("items");
            for(int i=0;i<jsonArray.length();i++){
                Map<String,Object> map = new HashMap<>();
                JSONObject obj = new JSONObject(jsonArray.get(i).toString());
                map.put("order_name",obj.getString("p_name"));
                map.put("order_price",obj.getString("price"));
                map.put("order_quantity",obj.getString("quantity"));
                items.add(map);
                totalprice+=Integer.parseInt(obj.getString("price"))*Integer.parseInt(obj.getString("quantity"));
            }
            simpleAdapter=new SimpleAdapter(this,items,R.layout.preview_order_list_style,new String[]{"order_name", "order_price","order_quantity"},
                    new int[]{R.id.order_name, R.id.order_price,R.id.order_quantity});
            listview.addHeaderView(View.inflate(this,R.layout.preview_list_header_style,null));
            listview.setAdapter(simpleAdapter);
            total.setText(totalprice+" 元");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

package com.ggfood.test.ggfood_api;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.ggfood.test.ggfood_api.database.IceBoxTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by s121 on 2017/10/30.
 */

public class PreviewOrder extends AppCompatActivity {
    public static String ITEM_FEILD = "data";
    private TextView textView[] = new TextView[4];
    private IceBoxTable.Item[] items;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        textView[0] = (TextView)findViewById(R.id.textView1);
        textView[1] = (TextView)findViewById(R.id.textView2);
        textView[2] = (TextView)findViewById(R.id.textView3);
        textView[3] = (TextView)findViewById(R.id.textView4);
        items = (IceBoxTable.Item[])getIntent().getSerializableExtra(ITEM_FEILD);
        StringBuilder[] sb = new StringBuilder[4];
        for(int i=0;i<4;i++)
            sb[i] = new StringBuilder(10);
        for(int i=0;i<items.length;i++){
            sb[0].append(items[i].id).append(',');
            sb[1].append(items[i].name).append(',');
            sb[2].append(items[i].expire).append(',');
            sb[3].append(items[i].quantity).append(',');
        }
        for(int i=0;i<4;i++) {
            textView[i].setText(sb[i].toString());
        }
//        List<Map<String, String>> items = new ArrayList<Map<String,String>>();
//        for(int i=0;i<this.items.length;i++){
//            Map<String, String> item = new HashMap<String, String>();
//            item.put("id",this.items[i].id);
//            item.put("name",this.items[i].name);
//            items.add(item);
//        }
//        SimpleAdapter simpleAdapter = new SimpleAdapter(this,
//                items,R.layout.food_listview_style,new String[]{"id","name"},new int[]{R.id.history_name,R.id.weight});
    }

}

package com.ggfood.test.ggfood_api.fragment;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.PreviewRecipe;
import com.ggfood.test.ggfood_api.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class History extends BaseFragment {
    ListView listView;
    SimpleAdapter simpleAdapter;
    List<Map<String,Object>> mapList;
    ArrayList<Integer> ids = new ArrayList();
    SQLiteDatabase sqLiteDatabase;
    @Override
    protected void initView(View rootView) {
        createDatabase();
        listView= (ListView) rootView.findViewById(R.id.history_list);
        mapList=new ArrayList<Map<String,Object>>();
        Cursor cursor= sqLiteDatabase.rawQuery("SELECT * FROM history",null);
        if(cursor.getCount()!=0){
            cursor.moveToFirst();
            do{
                Map<String,Object> map=new HashMap<>();
                map.put("name",cursor.getString(1));
                ids.add(cursor.getInt(2));
                map.put("date",cursor.getString(3));
                mapList.add(map);
            }while (cursor.moveToNext());
            simpleAdapter=new SimpleAdapter(getActivity(),mapList,R.layout.list_history,new String[]{"name", "date"},
                    new int[]{R.id.history_name, R.id.history_date});
            listView.setAdapter(simpleAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Toast.makeText(getActivity(),ids.get(position).toString(),Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getActivity(), PreviewRecipe.class);
                    intent.putExtra("id",ids.get(position).intValue());
                   getActivity().startActivity(intent);
                }
            });
        }
    }
    private void createDatabase(){
        final String dbname="ggfood";
        final String history_table="history";
        sqLiteDatabase=getActivity().openOrCreateDatabase(dbname, Context.MODE_PRIVATE,null);
        String createTable="CREATE TABLE IF NOT EXISTS "+history_table+" (id INT AUTO_INCREMENT,recipe_name VARCHAR(100),recipe_id INT,date Date,PRIMARY KEY (id));";
        sqLiteDatabase.execSQL(createTable);
    }
    @Override
    protected int getMainLayoutId() {
        return R.layout.fragment_history;
    }

}

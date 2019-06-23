package com.ggfood.test.ggfood_api.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.ggfood.test.ggfood_api.IceBoxAddActivity;
import com.ggfood.test.ggfood_api.R;
import com.ggfood.test.ggfood_api.database.FoodDataBase;
import com.ggfood.test.ggfood_api.database.IceBoxTable;
import com.ggfood.test.ggfood_api.database.IceBoxTable.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by s121 on 2017/6/1.
 */

public class IceBox extends BaseFragment {

    public static final int FORM_RESULT_CODE = 1; //表單結果
    private FoodDataBase db;
    private IceBoxTable table;
    private Handler mHandler = new Handler();
    private Thread thread = null;
    private FloatingActionButton bt_addItem;

    private MyAdapter itemAdapter;
    private RecyclerView recyclerView;
    @Override
    protected void initView(View rootView) {
        recyclerView = (RecyclerView) rootView.findViewById(R.id.show_result);
        itemAdapter = new MyAdapter(getActivity());

        final LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(itemAdapter);

        bt_addItem = (FloatingActionButton) rootView.findViewById(R.id.floatingActionButton2);
        bt_addItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), IceBoxAddActivity.class);
               IceBox.this.startActivityForResult(intent, FORM_RESULT_CODE);
            }
        });

        db = FoodDataBase.getDB(getActivity());
        table = db.getIceBoxTable();
        startInquiry();
        if(getArguments()!=null){
            if(getArguments().getBoolean("auto_start_qr_scan",false)){
                toAddActivity();
            }
        }

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void toAddActivity(){
        Intent intent = new Intent(getActivity(), IceBoxAddActivity.class);
        intent.putExtra("auto_start_qr_scan",true);
        startActivityForResult(intent, FORM_RESULT_CODE);
    }
    @Override
    protected int getMainLayoutId() {
        return R.layout.fragment_ice_box;
    }

    private void startInquiry(){
        if(thread == null || !thread.isAlive()) {
            thread = new Thread(new QueryTask());
        }else{
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            thread = new Thread(new QueryTask());
        }
        thread.start();
    }

    private void addData(Item item){
        itemAdapter.addItem(item);
        itemAdapter.notifyDataSetChanged();
        long r = table.insert(item);
        Log.e("ADD","ADD : rowid="+r);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == FORM_RESULT_CODE){
            if(resultCode == Activity.RESULT_OK) {
                Item item = new Item(data);
                addData(item);
                Log.e("IceBox", item.toString());
            }
        }
    }
    private class RefreshTask implements Runnable{
        @Override
        public void run() {
            itemAdapter.notifyDataSetChanged();
        }
    }
    private class QueryTask implements Runnable{
        @Override
        public void run() {
            Cursor c = table.selectAll();
            Log.e("QQ","queryTask");
            if(c.getCount() > 0) {
                c.moveToFirst();
                do {
                    //TODO 加入recycleView
                    itemAdapter.addItem(new Item(c));
                    Log.e("QQ", "queryTask++");
                } while (c.moveToNext());
            }
            mHandler.post(new RefreshTask());
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
        LayoutInflater mInflater;
        private List<Item> itemList;


        public MyAdapter(Context context){
            this(context,new ArrayList<Item>());
        }

        public MyAdapter(Context context, List<Item> list){
            mInflater = LayoutInflater.from(context);
            itemList = list;
        }
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = mInflater.inflate(R.layout.ice_box_item,parent,false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            Item item = itemList.get(position);
            holder.bind(item);
        }


        @Override
        public int getItemCount() {
            return itemList.size();
        }

        public Item getItem(int position){
            return itemList.get(position);
        }
        public void addItem(Item item){

            itemList.add(item);
        }
        public void removeItem(int position){
            itemList.remove(position);
        }
        public class ViewHolder extends RecyclerView.ViewHolder {
            Item item;
            TextView name;
            TextView expire;
            ImageButton plus;
            ImageButton minus;
            ImageButton delete;
            EditText ed_number;
            OnClickListener listener;

            public ViewHolder(View itemView) {
                super(itemView);
                name = (TextView) itemView.findViewById(R.id.recommend_name);
                expire = (TextView) itemView.findViewById(R.id.expire);
                ed_number = (EditText) itemView.findViewById(R.id.edit_text);
                plus = (ImageButton) itemView.findViewById(R.id.plus);
                minus = (ImageButton) itemView.findViewById(R.id.minus);
                delete = (ImageButton)itemView.findViewById(R.id.delete_btn);

                listener = new OnClickListener();

                plus.setOnClickListener(listener);
                minus.setOnClickListener(listener);
                delete.setOnClickListener(listener);
            }
            public void bind(Item item){
                this.item = item;
                name.setText(item.name);
                expire.setText(item.expire);
                ed_number.setText(String.valueOf(item.quantity));
            }
            private class OnClickListener implements View.OnClickListener{
                @Override
                public void onClick(View v) {
                    switch (v.getId()) {
                        case R.id.plus:
                            item.quantity += 1;
                            table.updateQuantity(item.id, item.quantity);
                            ed_number.setText(String.valueOf(item.quantity));
                            break;
                        case R.id.minus:
                            if(item.quantity > 0)
                                item.quantity -= 1;
                            table.updateQuantity(item.id, item.quantity);
                            ed_number.setText(String.valueOf(item.quantity));
                            break;
                        case R.id.delete_btn:
                            itemList.remove(item);
                            table.deleteById(item.id);
                            MyAdapter.this.notifyDataSetChanged();
                            break;
                    }
                }
            }
        }
    }

}


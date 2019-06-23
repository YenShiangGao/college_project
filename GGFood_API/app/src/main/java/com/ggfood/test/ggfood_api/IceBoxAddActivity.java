package com.ggfood.test.ggfood_api;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.ggfood.test.ggfood_api.tool.QRScanTool;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class IceBoxAddActivity extends AppCompatActivity implements View.OnClickListener {
    IntentIntegrator integrator;
    EditText id,name,quantity,deadline;
    DatePickerDialog datePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ice_box_add);
        getSupportActionBar().hide();

        findViewById(R.id.back).setOnClickListener(this);
        findViewById(R.id.qr_scan).setOnClickListener(this);
        findViewById(R.id.submit).setOnClickListener(this);
        findViewById(R.id.pick_time).setOnClickListener(this);

        id = (EditText)findViewById(R.id.ID);
        name = (EditText)findViewById(R.id.recommend_name);
        quantity = (EditText)findViewById(R.id.quantity);
        deadline = (EditText)findViewById(R.id.deadline);
        integrator = QRScanTool.createIntentIntegratorFactory(this);

        Calendar c = Calendar.getInstance();
        int mYear = c.get(Calendar.YEAR);
        int mMonth = c.get(Calendar.MONTH);
        int mDay = c.get(Calendar.DAY_OF_MONTH);
        datePicker = new DatePickerDialog(IceBoxAddActivity.this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                setDateFormat(year,month,day);
            }
        }, mYear,mMonth,mDay);

        boolean start_qr_scan = getIntent().getBooleanExtra("auto_start_qr_scan",false);
        if(start_qr_scan) {
            integrator.initiateScan();
        }
//        initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                try {
                    JSONObject obj = new JSONObject(result.getContents());
                    String _number = obj.getString("number");
                    String _name = obj.getString("name");
                    String _expire = obj.getString("expire");
                    id.setText(_number);
                    name.setText(_name);
                    deadline.setText(_expire);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

    }

    @Override
    public void onClick(View v) {
        int vid = v.getId();
        switch(vid){
            case R.id.submit:
                String sid = id.getText().toString();
                String strname = name.getText().toString();
                int intquantity = Integer.parseInt(quantity.getText().toString());
                String strdeadline = deadline.getText().toString();

                if(formCheck()){
                    Intent intent = new Intent();
                    intent.putExtra("ID", sid);
                    intent.putExtra("NAME", strname);
                    intent.putExtra("QUANTITY", intquantity);
                    intent.putExtra("EXPIRE", strdeadline);
                    setResult(RESULT_OK, intent);
                    finish();
                }else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            break;
            case R.id.back:
                setResult(RESULT_CANCELED);
                finish();
            break;
            case R.id.qr_scan:
                integrator.initiateScan();
            break;
            case R.id.pick_time:
                datePicker.show();
            break;
        }
    }

    boolean formCheck(){
        return true;
    }
    private void setDateFormat(int year,int monthOfYear,int dayOfMonth){
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR,year);
        c.set(Calendar.MONTH,monthOfYear);
        c.set(Calendar.DAY_OF_MONTH,dayOfMonth);
        deadline.setText(DateFormat.format("yyyy/MM/dd", c.getTime()).toString());
    }
}

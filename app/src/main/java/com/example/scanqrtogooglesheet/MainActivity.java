package com.example.scanqrtogooglesheet;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class MainActivity extends AppCompatActivity {

    EditText txt_qty, txt_employeeID;
    Spinner txt_process;
    Button btn_scan ,txt_date, btn_save;
    String scannedData, storeData1, storeData2, storeData3;
    TextView txt_name, txt_result;
    ImageButton btn_searchID;
    RetrofitAPI retrofitAPI;

    private DatePickerDialog datePickerDialog;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initDatePicker();

        txt_date = findViewById(R.id.txt_date);
        txt_qty = findViewById(R.id.txt_qty);
        txt_employeeID = findViewById(R.id.txt_emp);
        txt_process = findViewById(R.id.spn_process);
        txt_result = findViewById(R.id.txt_result);
        txt_name = findViewById(R.id.txt_name);
        btn_scan = findViewById(R.id.btn_scan);
        btn_save = findViewById(R.id.btn_save);
        btn_searchID = findViewById(R.id.btn_search);

        //txt_date.setText(getTodayDate());

        scannedData = null;
        txt_date.setText("");
        txt_qty.setText("");
        txt_employeeID.setText("");


        btn_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startScanning();
                txt_result.setText("");

            }
        });

        btn_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (scannedData == null){
                    Toast.makeText(MainActivity.this, "กรุณาสแกน QR Code", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (txt_process.getSelectedItem() == null){
                    Toast.makeText(MainActivity.this, "กรุณาเลือก Process", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (txt_date.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "กรุณาเลือกวันที่ผลิต", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (txt_qty.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "กรุณากรอกจำนวน", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (txt_employeeID.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "กรุณากรอกรหัสพนักงาน", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (txt_name.getText().toString().isEmpty()) {
                    Toast.makeText(MainActivity.this, "กรุณากดค้นหาพนักงาน", Toast.LENGTH_SHORT).show();
                    return;
                }

                new SendRequest().execute();

                int delayMillis = 2000;   //2sec
                btn_save.setEnabled(false);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        btn_save.setEnabled(true);
                    }
                },delayMillis);
            }
        });

        btn_searchID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txt_name.setText("");

                Retrofit retrofit = new Retrofit.Builder().baseUrl("https://script.google.com/macros/s/AKfycbwiDcKG7iEVtVkFrhILHGsRTbVNhOnnpqSua6fC055RYScSI7n9gQn6SJ-bW0Ue1UtJ/")
                        .addConverterFactory(new NullOnEmptyConverterFactory())
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                retrofitAPI = retrofit.create(RetrofitAPI.class);
                Call<List<Model_emp>> callEmp = retrofitAPI.getEmployeeById(txt_employeeID.getText().toString());
                callEmp.enqueue(new Callback<List<Model_emp>>() {
                    @Override
                    public void onResponse(Call<List<Model_emp>> call, Response<List<Model_emp>> response) {
                        if(response.isSuccessful()) {
                            List<Model_emp> model_emps = response.body();
                            for (Model_emp get : model_emps) {
                                txt_name.setText(get.getName());
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<List<Model_emp>> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "รหัสพนักงานไม่ถูกต้อง", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
//    private String getTodayDate(){
//        Calendar cal = Calendar.getInstance();
//        int year = cal.get(Calendar.YEAR);
//        int month = cal.get(Calendar.MONTH);
//        month = month + 1;
//        int day = cal.get(Calendar.DAY_OF_MONTH);
//        return makeDateSring(day, month, year);
//    }
    private void initDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int day) {
                month = month + 1;
                String date = makeDateSring(day, month, year);
                txt_date.setText(date);
            }
        };

        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int day = cal.get(Calendar.DAY_OF_MONTH);

        int style = AlertDialog.THEME_HOLO_LIGHT;

        datePickerDialog = new DatePickerDialog(this, style, dateSetListener, year, month, day);
    }

    private String makeDateSring(int day, int month, int year){
        return  day + "/" + getMonthFormat(month) + "/" + year;
    }
    private String getMonthFormat(int month){
        if(month == 1)
            //return "มกราคม";
            return "01";
        if(month == 2)
            //return "กุมภาพันธ์";
            return "02";
        if(month == 3)
            //return "มีนาคม";
            return "03";
        if(month == 4)
            //return "เมษายน";
            return "04";
        if(month == 5)
            //return "พฤษภาคม";
            return "05";
        if(month == 6)
            //return "มิถุนายน";
            return "06";
        if(month == 7)
            //return "กรกฎาคม";
            return "07";
        if(month == 8)
            //return "สิงหาคม";
            return "08";
        if(month == 9)
            //return "กันยายน";
            return "09";
        if(month == 10)
            //return "ตุลาคม";
            return "10";
        if(month == 11)
            //return "พฤศจิกายน";
            return "11";
        if(month == 12)
            //return "ธันวาคม";
            return "12";

        return "01"; //defult should never happen

    }

    public void openDatePicker(View view){
        datePickerDialog.show();
    }

    private void startScanning(){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan a QR code");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
        integrator.setOrientationLocked(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null) {
            scannedData = result.getContents();

            if (scannedData != null) {

                // Here we need to handle scanned data...
                // new SendRequest().execute(); //ส่งข้อมูล
                if(scannedData.contains("|")){
                    String[] inputArr = scannedData.split("\\|");
                        storeData1 = inputArr[0];
                        storeData2 = inputArr[1];
                        storeData3 = inputArr[2];
                    Log.d("TEST",scannedData);
                    Log.d("TEST",storeData1 + " " + storeData2 + " " + storeData3);
                    txt_result.setText("Job: " + storeData2 + "\n"+ "Part No: " + storeData3);
                }else if(scannedData.contains(",")){
                    String[] inputArrV2 = scannedData.split(",");
                        storeData1 = inputArrV2[0];
                        storeData2 = inputArrV2[1];
                        storeData3 = inputArrV2[2];
                    Log.d("TEST",scannedData);
                    txt_result.setText("Job: " + storeData2 + "\n"+ "Part No: " + storeData3);
                }else {
                    Toast.makeText(MainActivity.this, "Pattern QR incorrect", Toast.LENGTH_LONG).show();
                }
            }
            Retrofit retrofit = new Retrofit.Builder().baseUrl("https://script.google.com/macros/s/AKfycbyeh9Df3by1_iOgzLknaWGgp_v8DLdrDA8m0IRD7_2xi0rao8CJnK4tEtByHZtJq6WzLA/")
                    .addConverterFactory(new NullOnEmptyConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            retrofitAPI = retrofit.create(RetrofitAPI.class);
            Call<List<Model_item>> callitem = retrofitAPI.getGoogleSheet(storeData3);
            callitem.enqueue(new Callback<List<Model_item>>() {
                @Override
                public void onResponse(Call<List<Model_item>> call, Response<List<Model_item>> response) {
                    if(response.isSuccessful()) {
                        if(response.isSuccessful()){
                            List<Model_item> model_items = response.body();
                            if (model_items != null){
                                populateSpinner(model_items);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<Model_item>> call, Throwable t) {
                    Toast.makeText(MainActivity.this, "Dropdown erorr: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }
        int delayMillis = 1000;   //1sec
        btn_scan.setEnabled(false);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() { btn_scan.setEnabled(true); }
        },delayMillis);


    }

    public class SendRequest extends AsyncTask<String, Void, String> {

        protected void onPreExecute(){}

        protected String doInBackground(String... arg0) {

            try{

                //Enter script URL Here
                URL url = new URL("https://script.google.com/macros/s/AKfycbxL9ZAukSB0PRp2J1c4aaSuvBJ_fpd6fbSmbSSZ-OJM3XwBDrj_JodRRq-GYCkS6KE-/exec");

                JSONObject postDataParams = new JSONObject();


                postDataParams.put("dataQR",storeData1);
                postDataParams.put("dataQR1",storeData2);
                postDataParams.put("dataQR2",storeData3);
                postDataParams.put("sdata1",txt_date.getText().toString());
                postDataParams.put("sdata2",txt_process.getSelectedItem().toString());
                postDataParams.put("sdata3",txt_qty.getText().toString());
                postDataParams.put("sdata4",txt_employeeID.getText().toString());


                Log.d("params",postDataParams.toString());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(getPostDataString(postDataParams));

                writer.flush();
                writer.close();
                os.close();

                int responseCode=conn.getResponseCode();

                if (responseCode == HttpsURLConnection.HTTP_OK) {

                    BufferedReader in=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuffer sb = new StringBuffer("");
                    String line="";

                    while((line = in.readLine()) != null) {

                        sb.append(line);
                        break;
                    }

                    in.close();
                    return sb.toString();

                }
                else {
                    return new String("false : " + responseCode);
                }
            }
            catch(Exception e){
                return new String("Exception: " + e.getMessage());
            }
        }

        @Override
        protected void onPostExecute(String result) {
            scannedData = null;
            txt_date.setText("");
            txt_qty.setText("");
            txt_employeeID.setText("");
            txt_name.setText("");
            txt_result.setText("");
            txt_process.setAdapter(null);
            Toast.makeText(getApplicationContext(), result, Toast.LENGTH_LONG).show();

        }
    }

    public String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        boolean first = true;

        Iterator<String> itr = params.keys();

        while(itr.hasNext()){

            String key= itr.next();
            Object value = params.get(key);

            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));

        }
        return result.toString();
    }

    private void populateSpinner(List<Model_item> model_items){
        List<String> model_item = new ArrayList<>();
        for (Model_item process : model_items ){
            model_item.add(process.getProcess());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, model_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        txt_process.setAdapter(adapter);
    }

}
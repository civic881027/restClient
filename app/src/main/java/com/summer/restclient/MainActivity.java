package com.summer.restclient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.icu.text.Edits;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.summer.restclient.data.DBhelper;


import java.io.BufferedReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private Thread thread;
    private Socket clientSocket;
    private Handler handler=new Handler();
    private String tmp;
    private String orderString;
    private TextView tvTest;
    private SQLiteDatabase mDb;
    private RecyclerView rv;
    private orderAdapter adapter;
    private Button btnPush;
    private Button btnAccept;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tmp="";
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTest=(TextView)findViewById(R.id.tvOrderContent);
        rv=(RecyclerView)findViewById(R.id.rvOrder);
        btnPush=(Button)findViewById(R.id.btnPush);
        btnAccept=(Button)findViewById(R.id.btnAccept);
        DBhelper dBhelper=new DBhelper(this);
        mDb=dBhelper.getWritableDatabase();
        dBhelper.onUpgrade(mDb,1,2);

        thread=new Thread(getDataConnection);
        thread.start();

        adapter=new orderAdapter(this,getOrderForm());
        adapter.setOnItemClickListener(new orderAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Cursor foodCursor=getFoods(Integer.parseInt(view.getTag().toString()));
                System.out.println(view.getTag());

                String content= "";
                for(int i=0;i<foodCursor.getCount();i++){
                    System.out.println(i);
                    foodCursor.moveToPosition(i);
                    content=content+"food:"+foodCursor.getString(foodCursor.getColumnIndex("foodName"))+"\n";
                    content=content+"Count:"+String.valueOf(foodCursor.getInt(foodCursor.getColumnIndex("foodCount")))+"\n";
                }
                tvTest.setText(content);

            }
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);
    }


    private Runnable getDataConnection=new Runnable() {
        @Override
        public void run() {
            try{
                InetAddress serverIP=InetAddress.getByName("10.0.2.2");
                int serverPort=5050;
                clientSocket=new Socket(serverIP,serverPort);
                BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(),"BIG5"));
                bw.write("restClient\n");
                bw.flush();
                BufferedReader br=new BufferedReader(new InputStreamReader(clientSocket.getInputStream(),"BIG5"));
                while(clientSocket.isConnected()){
                    tmp=br.readLine();
                    if(tmp!=null){
                        if(tmp.contains("送餐完畢")){
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this,"送餐完成!!",Toast.LENGTH_LONG).show();
                                }
                            });
                        }else{
                            parseStringToDB();
                            orderString=tmp;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.changeCursor(getOrderForm());
                                }
                            });
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.e("ERROR","Socket="+e.toString());
                finish();
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void parseStringToDB(){
        Log.d("test","START!");

        int ID=0;
        int total=0;
        String restAddress="";
        String targetAddress="";
        ArrayList<String> foodName=new ArrayList<>();
        ArrayList<Integer> foodCount=new ArrayList<>();
        String[] temp=tmp.split(" ");

        for(int i=0;i<temp.length;i++){
            String n=temp[i];
            if(n.contains("ID:"))ID=Integer.parseInt(n.substring(n.indexOf("ID:")+3));
            if(n.contains("Name:"))foodName.add(n.substring(n.indexOf("Name:")+5));
            if(n.contains("Count:"))foodCount.add(Integer.parseInt(n.substring(n.indexOf("Count:")+6)));
            if(n.contains("Total:"))total=Integer.parseInt(n.substring(n.indexOf("Total:")+6));
            if(n.contains("restAddress"))restAddress=n.substring(n.indexOf("restAddress:")+12);
            if(n.contains("targetAddress:"))targetAddress=n.substring(n.indexOf("targetAddress:")+14);
        }
        Log.d("Values",String.valueOf(ID));
        Log.d("Values",String.valueOf(total));
        Log.d("Values",restAddress+"\n"+targetAddress);
        addNewOrder(ID,total,targetAddress);
        for(int i=0;i<foodName.size();i++){
            addNewFoods(foodName.get(i),foodCount.get(i),ID);
            Log.d("Values",foodName.get(i));
            Log.d("Values",String.valueOf(foodCount.get(i)));
        }
    }

    private long addNewOrder(int ID,int total,String address){
        ContentValues cv=new ContentValues();
        cv.put("_ID",ID);
        cv.put("total",total);
        cv.put("address",address);
        return mDb.insert("orderForm",null,cv);
    }
    private long addNewFoods(String food,int foodCount,int orderID){
        ContentValues cv=new ContentValues();
        cv.put("foodName",food);
        cv.put("foodCount",foodCount);
        cv.put("orderID",orderID);
        return mDb.insert("foods",null,cv);
    }
    private Cursor getOrderForm(){
        return mDb.rawQuery("SELECT _ID,address from orderForm",null);
    }
    private Cursor getFoods(int id){
        return mDb.rawQuery("SELECT _ID,foodName,foodCount from foods WHERE orderID="+id,null);
    }

    public void acceptOrder(View view){
        Thread pushThread=new Thread(pushDataConnection);
        pushThread.start();
        try{
            pushThread.join();
        }catch (InterruptedException e){
            e.printStackTrace();
        }

        btnAccept.setEnabled(false);
        btnAccept.setVisibility(Button.INVISIBLE);
        btnPush.setVisibility(View.VISIBLE);


    }
    public void pushOrder(View view){

        Thread thread=new Thread(new Runnable() {
            @Override
            public void run() {
                pushMsg(orderString,"deliveryClient");
            }
        });
        thread.start();
    }

    private Runnable pushDataConnection=new Runnable() {
        @Override
        public void run() {
            pushMsg("正在備餐中...","orderClient");
        }
    };
    private void pushMsg(String msg,String client){
        try{
            OutputStream os=clientSocket.getOutputStream();
            BufferedWriter bw=new BufferedWriter(new OutputStreamWriter(os,"BIG5"));
            while(clientSocket.isConnected()){
                bw.write(msg+" "+client+"\n");
                bw.flush();
                break;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}

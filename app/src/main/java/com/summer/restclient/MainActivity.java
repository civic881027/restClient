package com.summer.restclient;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private Thread thread;
    private Socket clientSocket;
    private Handler handler=new Handler();
    private String tmp;
    private TextView tvTest;
    private JSONObject jsonWrite,jsonRead;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        tmp=new String("");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvTest=(TextView)findViewById(R.id.test);
        thread=new Thread(Connection);
        thread.start();
    }

    private Runnable Connection=new Runnable() {
        @Override
        public void run() {
            try{
                InetAddress serverIP=InetAddress.getByName("10.0.2.2");
                int serverPort=5050;
                clientSocket=new Socket(serverIP,serverPort);
                BufferedReader br=new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), "BIG5"));
                while(clientSocket.isConnected()){
                    while(br.ready()){
                        tmp=tmp+br.readLine()+"\n";
                    }
                    if(tmp!=null&&!tmp.equals("")){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                tvTest.setText(tmp);
                            }
                        });
                    }
                }
                clientSocket.close();
                br.close();

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
}

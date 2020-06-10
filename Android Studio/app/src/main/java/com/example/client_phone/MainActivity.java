package com.example.client_phone;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.NetworkOnMainThreadException;
import android.speech.tts.TextToSpeech;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import static android.speech.tts.TextToSpeech.ERROR;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private String host = "172.20.10.10";
    private int port = 9999;
    private Socket client_Socket;
    private BufferedReader inputStream;
    private PrintWriter outputStream;

    //TTS---------------
    private TextToSpeech tts;
    //------------------

    TextView view1;
    TextView view2;
    TextView view3;
    TextView connectInfo;
    TextView checkInfo;
    TextView payInfo;


    EditText ipWriteText;
    EditText parkingCheckCarNum;
    EditText payCheckCarNum;

    Button socketConnectButton;
    Button parkingCheckButton;
    Button payCheckButton;

    ArrayList<String> carInfoList = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setTitle("주차 관리 시스템");

        view1 = (TextView) findViewById(R.id.view1);
        view2 = (TextView) findViewById(R.id.view2);
        view3 = (TextView) findViewById(R.id.view3);
        connectInfo = (TextView) findViewById(R.id.connectInfo);
        checkInfo = (TextView) findViewById(R.id.checkInfo);
        payInfo = (TextView) findViewById(R.id.payInfo);

        ipWriteText = (EditText) findViewById(R.id.ipWriteText);
        parkingCheckCarNum = (EditText) findViewById(R.id.carNumWrite1);
        payCheckCarNum = (EditText)findViewById(R.id.carNumWrite2);

        socketConnectButton = (Button)findViewById(R.id.send);
        parkingCheckButton = (Button)findViewById(R.id.sendCarNum1);
        payCheckButton = (Button)findViewById(R.id.sendCarNum2);

        socketConnectButton.setOnClickListener(this);
        parkingCheckButton.setOnClickListener(this);
        payCheckButton.setOnClickListener(this);
        carInfoList.add("1111A");
        carInfoList.add("2222B");
        carInfoList.add("3333C");

        //tts-------------------
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != ERROR){
                    tts.setLanguage(Locale.KOREAN);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            case R.id.action_btn1:
            case R.id.action_btn2:
            case R.id.action_btn3:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.send:
                String ip = ipWriteText.getText().toString();
                connectInfo.setText(ip);
//                long now = System.currentTimeMillis();
//                Date mDate = new Date(now);
//                @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDate = new SimpleDateFormat("hh:mm:ss");
//                String getTime = simpleDate.format(mDate);
//                connectInfo.setText(getTime);
                connect(ip);
                break;
            case R.id.sendCarNum1:

                boolean carCheckFlag = false;
                int index = 0;
                String carNum = parkingCheckCarNum.getText().toString();

                if(carInfoList != null){
                    for(int i = 0 ; i < carInfoList.size(); i++){
                        String storedCarNUm = carInfoList.get(i).substring(0,4);
                        if(carNum.equals(storedCarNUm)){
                            carCheckFlag = true;
                            index = i;
                        }
                    }
                }

                if(carCheckFlag){
                    //TTS
                    checkInfo.setText("차량번호 : " + carNum + ", 주차 구역 : " + carInfoList.get(index).substring(4));
                    if(carNum.substring(3).equals("2") || carNum.substring(3).equals("4") || carNum.substring(3).equals("5") || carNum.substring(3).equals("9"))
                        tts.speak("차량번호 " + carNum + ". 는." + carInfoList.get(index).substring(4) + "구역에 있습니다",TextToSpeech.QUEUE_FLUSH,null);
                    else
                        tts.speak("차량번호 " + carNum + ". 은." + carInfoList.get(index).substring(4) + "구역에 있습니다",TextToSpeech.QUEUE_FLUSH,null);
                }
                else{
                    //TTS
                    checkInfo.setText("조회되지 않는 차량입니다");
                    tts.speak("조회되지 않는 차량입니다",TextToSpeech.QUEUE_FLUSH,null);
                }

                break;
            case R.id.carNumWrite2:
                break;
        }
    }
//    @SuppressLint("HandlerLeak")
//    Handler mainHandler = new Handler(){
//        public void handleMessage(Message msg){
//            switch(msg.what){
//                case 1:
//                    recvText.setText(msg.obj.toString());
//                    System.out.println("Ssss");
//                    break;
//            }
//        }
//    };

    public void onDestroy(){
        super.onDestroy();
        try {
            if(client_Socket != null){
                client_Socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(tts != null) {

            tts.stop();
            tts.shutdown();
        }
    }

    void connect(final String address){
        Thread checkUpdate = new Thread() {
            public void run() {
                String ip = address;
                try {
                    client_Socket = new Socket(ip, port);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                try {
                    inputStream = new BufferedReader(new InputStreamReader(client_Socket.getInputStream()));
                    outputStream = new PrintWriter(client_Socket.getOutputStream(),true);
                } catch (IOException e) {
                    e.printStackTrace();
                }

//                try {
//                    outputStream.println("안녕 난 클라이언트야");
//
//                    String recv = inputStream.readLine();
//                    System.out.println(recv);
//                    Message msg = Message.obtain(null, 1, recv);
//                    mainHandler.sendMessage(msg);
//                    client_Socket.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }

                /*
                while(true){
                    서버에서 (4자리+주차자리)String 오면 split해 배열에 저장
                    안오면 올때까지 대기, 요금정산이랑 주차확인은 어차피 버튼으로 send시킴.

                }
                */
            }
        };
        checkUpdate.start();
    }

}
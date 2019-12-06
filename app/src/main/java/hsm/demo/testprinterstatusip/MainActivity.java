package hsm.demo.testprinterstatusip;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.util.Xml;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {

    TcpClient tcpClient=null;

    TextView tvIP, tvPort;
    TextView tvMessages;
    EditText etMessage;
    Button btnSend;
    public static String SERVER_IP = "158.138.39.112";
    public static int SERVER_PORT = 9100;
    String message;
    final int MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE=0014;
    static Handler mHandler=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                Message copy = Message.obtain(msg);
                super.handleMessage(msg);
                //do something with msg
                Bundle bundle=msg.getData();
                String m=bundle.getString("message");
                addMessage(m);
            }
        };
        tvIP=findViewById(R.id.tvIP);
        

        tvPort=findViewById(R.id.tvPort);
        tvPort.setText(""+SERVER_PORT);

        tvMessages=findViewById(R.id.tvMessages);
        tvMessages.setMovementMethod(new ScrollingMovementMethod());

        btnSend=findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View view) {
                                           SERVER_IP=tvIP.getText().toString();
                                           SERVER_PORT=Integer.parseInt(tvPort.getText().toString());
                                           getStatus _geGetStatus=new getStatus();
                                           _geGetStatus.setHandler(mHandler);
                                           StringBuilder sb=new StringBuilder();
//                                           sb.append("A$=SYSHEALTH$\r\n");
//                                           sb.append("PRINT A$\r\n");
//                                           sb.append("IMMEDIATE ON\r\n");
                                           sb.append("?PRSTAT\r\n");
                                           _geGetStatus.execute(SERVER_IP, ""+SERVER_PORT, sb.toString());
//                                           _geGetStatus.sendDataToNetwork(sb.toString().getBytes());
//                                           try {
//                                               EditText et = (EditText) findViewById(R.id.editText);
//                                               String str = et.getText().toString();
//                                               tcpClient.sendMessage(str+"\r\n");
//                                               addMessage("sent '" + str+"'");
//                                           } catch (Exception e) {
//                                               e.printStackTrace();
//                                           }
                                       }
                                   });
        getPermission();
    }

    private void startClient(){
        return;
//        tcpClient=new TcpClient(new TcpClient.OnMessageReceived() {
//            @Override
//            public void messageReceived(String message) {
//                addMessage(message);
//            }
//        });
//        tcpClient.run();
    }
    private void getPermission() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_WIFI_STATE},MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE);
            // lv.setAdapter(new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,lists));
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
            addMessage("requesting permission");
        }else{
            startClient();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) { //funcitno executes when some permission was granted
        if (requestCode == MY_PERMISSIONS_REQUEST_ACCESS_WIFI_STATE) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) { //check if permission was already grnated and start scannig if yes
                    addMessage("permission granted. Starting client socket");
                    startClient();
                    return;
                }
            }
            getPermission(); //ask for permission if not given
        }
    }

    void addMessage(final String m){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvMessages.append(m+"\n");
            }
        });
    }

}

package hsm.demo.testprinterstatusip;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class getStatus extends AsyncTask<String , String , Integer > { //params, progress, result
    Socket nsocket; //Network Socket
    InputStream nis; //Network Input Stream
    OutputStream nos; //Network Output Stream
    final String TAG="myAsyncTask";
    Handler mHandler=null;

    public void setHandler(Handler handler){
        mHandler=handler;
    }

    @Override
    protected Integer doInBackground(String... params){
        Log.i(TAG, "doInBackground");
        String sServer=params[0];
        String sPort=params[1];
        String sCmd=params[2];
        int iPort=Integer.parseInt(sPort);
        Integer result=-99;
        try {
            Log.i(TAG, "doInBackground: Creating socket");

            SocketAddress sockaddr = new InetSocketAddress(sServer, iPort);
            nsocket = new Socket();
            nsocket.connect(sockaddr, 5000); //10 second connection timeout
            if (nsocket.isConnected()) {
                nis = nsocket.getInputStream();
                nos = nsocket.getOutputStream();

                sendDataToNetwork("? PRSTAT".getBytes());
                Log.i("AsyncTask", "doInBackground: Socket created, streams assigned");
                Log.i("AsyncTask", "doInBackground: Waiting for inital data...");
                byte[] buffer = new byte[4096];
                int read = nis.read(buffer, 0, 4096); //This is blocking
                while(read != -1){
                    byte[] tempdata = new byte[read];
                    System.arraycopy(buffer, 0, tempdata, 0, read);
                    publishProgress(new String(tempdata));
                    publishProgress(printWithHex(tempdata));
                    Log.i(TAG, "doInBackground: Got some data: " + printWithHex(tempdata));
                    read = nis.read(buffer, 0, 4096); //This is blocking
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("AsyncTask", "doInBackground: IOException");
            publishProgress("IOException: "+e.getMessage());
            result = -1;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("AsyncTask", "doInBackground: Exception");
            publishProgress("Exception: "+e.getMessage());
            result = -2;
        } finally {
            try {
                if(nis!=null)
                    nis.close();
                if(nos!=null)
                    nos.close();
                if(nsocket!=null)
                    nsocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i("AsyncTask", "doInBackground: Finished");
            publishProgress("query: Finished");
        }
        return result;
    }

    @Override
    protected void onPreExecute() {
        Log.i(TAG, "onPreExecute");
    }

    public boolean sendDataToNetwork(final byte[] cmd) {
        if (nsocket!=null && nsocket.isConnected()) {
            Log.i(TAG, "SendDataToNetwork: Writing received message to socket");
            sendMsg("SendDataToNetwork: Writing received message to socket");
            new Thread(new Runnable() {
                public void run() {
                    try {
                        sendMsg("Sending: "+cmd.toString());
                        nos.write(cmd);
                    } catch (Exception e) {
                        e.printStackTrace();
                        sendMsg("SendDataToNetwork: Message send failed. Caught an exception: "+e.getMessage());
                        Log.i(TAG, "SendDataToNetwork: Message send failed. Caught an exception");
                    }
                }
            }).start();

            return true;
        }

        Log.i(TAG, "SendDataToNetwork: Cannot send message. Socket is closed");
        sendMsg("SendDataToNetwork: Cannot send message. Socket is closed: " + new String(cmd));
        return false;
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if (values.length > 0) {
            Log.i(TAG, "onProgressUpdate: " + values[0].length() + " bytes received.");
            //textStatus.setText(new String(values[0]));
            sendMsg(values[0]);
        }
    }

    void sendMsg(String message){
        Message msg=new Message();
        Bundle bundle=new Bundle();
        bundle.putString("message", message);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    String printWithHex(String sIn){
        StringBuilder sOut=new StringBuilder();
        // Step-1 - Convert ASCII string to char array
        char[] ch = sIn.toCharArray();

        for (char c : ch) {
            if(c<0x20 || c>0x7F) {
                // Step-2 Use %H to format character to Hex
                String hexCode = String.format("<0x%H (%d)>", c, (int)c);
                sOut.append(hexCode);
            }else
                sOut.append(c);
        }
        return  sOut.toString();
    }

    String printWithHex(byte[] bytes){
        StringBuilder sOut = new StringBuilder();
        for (byte b:bytes){
            if(b<0x20 || b>0x7F){
                String hexCode = String.format("<0x%H (%d)>", b, (int)b);
                sOut.append(hexCode);
            }else{
                sOut.append((char)b);
            }
        }
        return sOut.toString();
    }
    @Override
    protected void onCancelled() {
        Log.i(TAG, "Cancelled.");
        sendMsg("Canceled");
        //btnStart.setVisibility(View.VISIBLE);
    }

//    @Override
//    protected void onPostExecute(Boolean result) {
//        if (result) {
//            Log.i("AsyncTask", "onPostExecute: Completed with an Error.");
//            sendMsg("onPostExecute: Completed with an Error.");
//            //textStatus.setText("There was a connection error.");
//        } else {
//            Log.i("AsyncTask", "onPostExecute: Completed.");
//        }
////        btnStart.setVisibility(View.VISIBLE);
//    }
}

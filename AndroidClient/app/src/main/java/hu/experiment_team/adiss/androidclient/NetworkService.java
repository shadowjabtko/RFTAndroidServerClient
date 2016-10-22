package hu.experiment_team.adiss.androidclient;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class NetworkService extends Service {

    private static final String TAG = "NetworkService";
    private static final String EXTRA_SERVER_MESSAGE = "hu.experiment_team.adiss.androidclient.server_message";

    private final IBinder mBinder = new NetworkBinder();

    private String serverIp = "192.168.1.179";
    private int serverPort = 9967;

    private volatile Socket client = null;
    private volatile static ObjectOutputStream out = null;
    private volatile static ObjectInputStream in = null;

    private Handler handler = new Handler();

    class NetworkBinder extends Binder {
        NetworkService getService(){
            return NetworkService.this;
        }
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        new Thread(new TCPClientThread()).start();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    void sendMessage(Object message){
        class TCPClientWriteThread implements Runnable {
            private Object message;
            private TCPClientWriteThread(Object msg) {
                message = msg;
            }
            @Override
            public void run() {
                try {
                    out.writeObject(message.toString());
                    out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        new Thread(new TCPClientWriteThread(message)).start();
    }


    /*
    *   TCP Client implementation
    * */
    private class TCPClientThread implements Runnable {
        @Override
        public void run() {
            try{
                client = new Socket(serverIp, serverPort);
                out = new ObjectOutputStream(client.getOutputStream());
                in = new ObjectInputStream(client.getInputStream());
                Log.d(TAG, "Connected to " + serverIp);
            } catch (IOException e) {
                e.printStackTrace();
            }

            try{
                Object serverIn;
                while(true){
                    if((serverIn = in.readObject()) != null){
                        if(serverIn instanceof String){
                            final Object finalServerIn = serverIn;
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Intent response = new Intent(EXTRA_SERVER_MESSAGE);
                                    response.putExtra(EXTRA_SERVER_MESSAGE, (String)finalServerIn);
                                    LocalBroadcastManager.getInstance(NetworkService.this).sendBroadcast(response);
                                    Log.d(TAG, (String)finalServerIn);
                                }
                            });
                            Log.d(TAG, (String)serverIn);
                        }
                    } else {
                        break;
                    }
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }

        }
    }

    private boolean isNetworkAvailableAndConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        return isNetworkAvailable && cm.getActiveNetworkInfo().isConnected();
    }
}

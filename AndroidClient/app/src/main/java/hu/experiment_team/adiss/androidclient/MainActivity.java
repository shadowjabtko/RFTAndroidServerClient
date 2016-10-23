package hu.experiment_team.adiss.androidclient;

import android.content.*;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This class handles the users interactions with the user interface.
 * @see AppCompatActivity
 * @author Jakab Ádám
 * */
public class MainActivity extends AppCompatActivity {

    /**
     * Some constraints for the debugging and intent filtering.
     */
    private static final String TAG = "MainActivity";
    private static final String EXTRA_SERVER_MESSAGE = "hu.experiment_team.adiss.androidclient.server_message";
    private static final String EXTRA_NETWORK_NOT_AVAILABLE = "hu.experiment_team.adiss.androidclient.network_not_available";
    private static final String EXTRA_SERVER_NOT_RUNNING = "hu.experiment_team.adiss.androidclient.server_not_running";
    private static final String EXTRA_CANT_SEND_MESSAGE = "hu.experiment_team.adiss.androidclient.cant_send_message";

    /**
     * Receives the service's broadcast messages and handles them.
     * */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(EXTRA_SERVER_MESSAGE.equals(intent.getAction())){
                textViewSzervertol.setText(intent.getStringExtra(EXTRA_SERVER_MESSAGE));
                Log.d(TAG, "Recieved message by BroadcastReciever: " + intent.getStringExtra(EXTRA_SERVER_MESSAGE));
            }
            if(EXTRA_NETWORK_NOT_AVAILABLE.equals(intent.getAction())){
                textViewSzervertol.setText(intent.getStringExtra(EXTRA_NETWORK_NOT_AVAILABLE));
                Log.d(TAG, "Recieved message by BroadcastReciever: " + intent.getStringExtra(EXTRA_NETWORK_NOT_AVAILABLE));
            }
            if(EXTRA_SERVER_NOT_RUNNING.equals(intent.getAction())){
                textViewSzervertol.setText(intent.getStringExtra(EXTRA_SERVER_NOT_RUNNING));
                Log.d(TAG, "Recieved message by BroadcastReciever: " + intent.getStringExtra(EXTRA_SERVER_NOT_RUNNING));
            }
            if(EXTRA_CANT_SEND_MESSAGE.equals(intent.getAction())){
                textViewSzervertol.setText(intent.getStringExtra(EXTRA_CANT_SEND_MESSAGE));
                Log.d(TAG, "Recieved message by BroadcastReciever: " + intent.getStringExtra(EXTRA_CANT_SEND_MESSAGE));
            }
        }
    };

    /**
     * The bounded service and its state.
     * */
    NetworkService mService;
    boolean mBound = false;

    /**
     * Widgets of the user interface.
     * */
    private TextView textViewSzervertol;
    private EditText editTextSzervernek;
    private Button buttonKuldes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent(this, NetworkService.class);
        startService(intent);

        textViewSzervertol = (TextView) findViewById(R.id.textview_uzenet_a_szervertol);
        editTextSzervernek = (EditText) findViewById(R.id.userInputTextField);
        buttonKuldes = (Button) findViewById(R.id.button_submit_button);

        buttonKuldes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.sendMessage(editTextSzervernek.getText().toString());
            }
        });

        registerBroadcastReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, NetworkService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind from the service
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        Intent intent = new Intent(this, NetworkService.class);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        stopService(intent);
    }

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            NetworkService.NetworkBinder binder = (NetworkService.NetworkBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    /**
     * Registering the available intent extras to the filter.
     * */
    private void registerBroadcastReceiver(){
        IntentFilter filter = new IntentFilter(EXTRA_SERVER_MESSAGE);
        filter.addAction(EXTRA_NETWORK_NOT_AVAILABLE);
        filter.addAction(EXTRA_SERVER_NOT_RUNNING);
        filter.addAction(EXTRA_CANT_SEND_MESSAGE);
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, filter);
    }

}

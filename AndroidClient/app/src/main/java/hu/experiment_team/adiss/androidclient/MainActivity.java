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

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final String EXTRA_SERVER_MESSAGE = "hu.experiment_team.adiss.androidclient.server_message";

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            textViewSzervertol.setText(intent.getStringExtra(EXTRA_SERVER_MESSAGE));
            Log.d(TAG, "Recieved message by BroadcastReciever: " + intent.getStringExtra(EXTRA_SERVER_MESSAGE));
        }
    };

    NetworkService mService;
    boolean mBound = false;

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

        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(EXTRA_SERVER_MESSAGE));
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

}

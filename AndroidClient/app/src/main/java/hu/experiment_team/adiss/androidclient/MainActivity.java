package hu.experiment_team.adiss.androidclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    NetworkService mService;
    boolean mBound = false;

    private TextView textViewSzervertol;
    private EditText editTextSzervernek;
    private Button buttonKuldes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textViewSzervertol = (TextView) findViewById(R.id.textview_uzenet_a_szervertol);
        editTextSzervernek = (EditText) findViewById(R.id.userInputTextField);
        buttonKuldes = (Button) findViewById(R.id.button_submit_button);

        buttonKuldes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mService.sendMessage(editTextSzervernek.getText().toString());
            }
        });
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

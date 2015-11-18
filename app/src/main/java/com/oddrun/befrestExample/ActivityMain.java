package com.oddrun.befrestExample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.oddrun.befrest.Befrest;
import com.oddrun.befrest.BefrestPushBroadcastReceiver;


public class ActivityMain extends AppCompatActivity {
    public static final String TAG = "ActivityMain";

    TextView received;
    TextView count;
    ScrollView scrollView;
    Button send;
    EditText editText;

    BroadcastReceiver receiver = new Receiver();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        received = (TextView) findViewById(R.id.received);
        count = (TextView) findViewById(R.id.count);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        send = (Button) findViewById(R.id.send);
        editText = (EditText) findViewById(R.id.editText);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Befrest.sendMessage(editText.getText().toString());
            }
        });
//        for(int i = 0 ; i < 500; i++){
//            Befrest.sendMessage("" + i);
//        }
        registerReceiver(receiver, new IntentFilter(Befrest.ACTION_PUSH_RECIEVED));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    class Receiver extends BefrestPushBroadcastReceiver{

        @Override
        public void onPushReceived(Context context, String message) {
            received.setText(received.getText() + "\n" + message);
            count.setText(Integer.parseInt(count.getText().toString()) + 1 + "");
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.smoothScrollTo(0, scrollView.getBottom());
                }
            });
        }

        @Override
        public void onAuthorizeProblem(Context context) {
            // re initial
        }
    }
}

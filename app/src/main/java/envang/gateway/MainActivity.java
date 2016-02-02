package envang.gateway;

import android.content.ContentResolver;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    TextView tv_ipAddress;
    CheckBox cb_runServer;
    TextView tv_serverLog;
    SmsGetWayServer server;
    String ipAddress;
    String strServerLog = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tv_ipAddress = (TextView)findViewById(R.id.tv_ipAddress);
        cb_runServer = (CheckBox)findViewById(R.id.cb_runServer);
        tv_serverLog = (TextView)findViewById(R.id.tv_serverLog);
        ipAddress = Utils.getIPAddress(true);
        tv_ipAddress.setText("http://" + ipAddress + ":2000");
        server = new SmsGetWayServer(ipAddress, 2000, tv_serverLog, this);
        cb_runServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb_runServer.isChecked()) {
                    try {
                        server.start();
                        cb_runServer.setText("Stop");
                        strServerLog = "SMS server running at http://" + ipAddress + ":2000\n";
                        tv_serverLog.append(strServerLog);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    strServerLog = "SMS server stopped\n";
                    tv_serverLog.append(strServerLog);
                    server.stop();
                    cb_runServer.setText("Run");
                }
            }
        });

        if (savedInstanceState !=null) {
            Boolean isChecked = savedInstanceState.getBoolean("Run");
            if (isChecked)
                cb_runServer.setText("Stop");
            else
                cb_runServer.setText("Run");
            cb_runServer.setChecked(isChecked);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putBoolean("Run", cb_runServer.isChecked() );
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}

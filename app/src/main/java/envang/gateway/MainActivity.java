package envang.gateway;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;

public class MainActivity extends Activity {
    TextView tv_ipAddress;
    Button btn_startServer;
    TextView tv_serverLog;
    String strServerLog = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_ipAddress = (TextView)findViewById(R.id.tv_ipAddress);
        btn_startServer = (Button)findViewById(R.id.btn_Start);
        tv_serverLog = (TextView)findViewById(R.id.tv_serverLog);
        btn_startServer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String ipAddress = Utils.getIPAddress(true);
                tv_ipAddress.setText("http://" + ipAddress+":2000");
                SmsGetWayServer server = new SmsGetWayServer(ipAddress, 2000);
                try {
                    server.start();
                    strServerLog = strServerLog + "SMS server running on at http://" + ipAddress + ":2000\n";
                    tv_serverLog.setText(strServerLog);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
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

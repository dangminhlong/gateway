package envang.gateway;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.PersistableBundle;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    TextView tv_ipAddress;
    CheckBox cb_runServer;
    TextView tv_serverLog;
    SmsGetWayServer server;
    String ipAddress;
    String strServerLog = "";
    Boolean bRunning;

    private BroadcastReceiver sentMessageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    Toast.makeText(context,
                            "SMS Sent" + intent.getIntExtra("object", 0),
                            Toast.LENGTH_SHORT).show();

                    break;
                case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                    Toast.makeText(context, "SMS generic failure", Toast.LENGTH_SHORT)
                            .show();

                    break;
                case SmsManager.RESULT_ERROR_NO_SERVICE:
                    Toast.makeText(context, "SMS no service", Toast.LENGTH_SHORT)
                            .show();

                    break;
                case SmsManager.RESULT_ERROR_NULL_PDU:
                    Toast.makeText(context, "SMS null PDU", Toast.LENGTH_SHORT).show();
                    break;
                case SmsManager.RESULT_ERROR_RADIO_OFF:
                    Toast.makeText(context, "SMS radio off", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private  BroadcastReceiver deliveredMessageBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (getResultCode()) {
                case Activity.RESULT_OK:
                    String to = intent.getStringExtra("To");
                    String message = intent.getStringExtra("Message");
                    //delSmsFromInbox(to, message);
                    Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bRunning = false;
        tv_ipAddress = (TextView)findViewById(R.id.tv_ipAddress);
        cb_runServer = (CheckBox)findViewById(R.id.cb_runServer);
        tv_serverLog = (TextView)findViewById(R.id.tv_serverLog);
        ipAddress = Utils.getIPAddress(true);
        tv_ipAddress.setText("http://" + ipAddress + ":2000");
        SmsHandler smsHandler = new SmsHandler();
        server = new SmsGetWayServer(ipAddress, 2000, tv_serverLog, smsHandler);
        cb_runServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb_runServer.isChecked()) {
                    try {
                        server.start();
                        bRunning = true;
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
                    bRunning = false;
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
    protected void onResume() {
        super.onResume();
        this.cb_runServer.setChecked(bRunning);
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

    public class SmsHandler{
        private final String SMS_CONTENT_URI = "content://sms/conversations/";
        private final String SMS_CONTENT_INBOX = "content://sms/inbox";
        private final String SMS_CONTENT_SENT = "content://sms/sent";
        private String SENT = "SMS_SENT";
        private final String SENT_SMS_BUNDLE = "sent";
        private String DELIVERED = "SMS_DELIVERED";
        private final String DELIVERED_SMS_BUNDLE = "delivered";
        private SmsManager smgr;
        private String str_log;
        private Context mContext;
        public SmsHandler(){
            mContext = getApplicationContext();
            this.smgr = SmsManager.getDefault();
            this.mContext.registerReceiver(sentMessageBroadcastReceiver, new IntentFilter(SENT));
            this.mContext.registerReceiver(deliveredMessageBroadcastReceiver, new IntentFilter(DELIVERED));
        }

        public void sendSMS(String to, String message){
            ArrayList<PendingIntent> sentIntents = new ArrayList<>();
            ArrayList<PendingIntent> deliveryIntents = new ArrayList<>();
            ArrayList<String> parts = this.smgr.divideMessage(message);
            for (int i = 0; i < parts.size(); i++) {
                Intent sentMessageIntent = new Intent(SENT);
                sentMessageIntent.putExtra(SENT_SMS_BUNDLE, message);
                PendingIntent sentIntent = PendingIntent
                        .getBroadcast(mContext, (int) System.currentTimeMillis(), sentMessageIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                Intent delivered = new Intent(DELIVERED);
                delivered.putExtra("To", to);
                delivered.putExtra("Message", parts.get(i));
                delivered.putExtra(DELIVERED_SMS_BUNDLE, message);
                PendingIntent deliveryIntent = PendingIntent
                        .getBroadcast(mContext, (int) System.currentTimeMillis(), delivered,
                                PendingIntent.FLAG_UPDATE_CURRENT);

                sentIntents.add(sentIntent);
                deliveryIntents.add(deliveryIntent);
            }
            this.smgr.sendMultipartTextMessage(to, null, parts, sentIntents, deliveryIntents);
        }

        public boolean delSmsFromInbox(String messageFrom, String messageBody) {
            final long threadId = getThreadId(messageFrom, messageBody);
            Uri smsUri = Util.isKitKatOrHigher() ? ContentUris
                    .withAppendedId(SmsQuery.SMS_CONVERSATION_URI,
                            threadId)
                    : ContentUris.withAppendedId(Uri.parse(SMS_CONTENT_URI), threadId);
            if (threadId >= 0) {

                int rowsDeleted = mContext.getContentResolver().delete(
                        smsUri, null, null);
                if (rowsDeleted > 0) {
                    return true;
                }
                return false;
            }
            return false;
        }

        private long getThreadIdKitKat(String messageFrom, String messageBody) {
            StringBuilder sb = new StringBuilder();
            sb.append(
                    Telephony.Sms.Inbox.ADDRESS + "=" + DatabaseUtils
                            .sqlEscapeString(messageFrom)
                            + " AND ");
            sb.append(Telephony.Sms.Inbox.BODY + "=" + DatabaseUtils
                    .sqlEscapeString(messageBody));
            Cursor c = mContext.getContentResolver()
                    .query(SmsQuery.INBOX_CONTENT_URI, SmsQuery.PROJECTION, sb.toString(), null,
                            SmsQuery.SORT_ORDER);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    long threadId = c.getLong(c.getColumnIndex(Telephony.Sms.Inbox.THREAD_ID));
                    c.close();
                    return threadId;
                }
                c.close();
            }
            return 0;
        }

        private long getThreadId(String messageFrom, String messageBody) {
            if (Util.isKitKatOrHigher()) {
                return getThreadIdKitKat(messageFrom, messageBody);
            }
            //Uri uriSms = Uri.parse(SMS_CONTENT_INBOX);
            StringBuilder sb = new StringBuilder();
            sb.append("address=" + DatabaseUtils.sqlEscapeString(messageFrom) + " AND ");
            sb.append("body=" + DatabaseUtils.sqlEscapeString(messageBody));
            Cursor c = mContext.getContentResolver().query(SmsQuery.INBOX_CONTENT_URI, null, sb.toString(), null,
                    "date DESC ");
            if (c != null) {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    long threadId = c.getLong(c.getColumnIndex("thread_id"));
                    c.close();
                    return threadId;
                }
                c.close();
            }
            return 0;
        }
    }

    @SuppressLint("NewApi")
    private interface SmsQuery {

        Uri INBOX_CONTENT_URI = Telephony.Sms.Inbox.CONTENT_URI;
        Uri SENT_CONTENT_URI = Telephony.Sms.Sent.CONTENT_URI;
        Uri SMS_CONVERSATION_URI = Telephony.Sms.Conversations.CONTENT_URI;
        String[] PROJECTION = {
                Telephony.Sms.Inbox._ID,
                Telephony.Sms.Inbox.ADDRESS,
                Telephony.Sms.Inbox.BODY,
                Telephony.Sms.Inbox.DATE,
        };
        String SORT_ORDER = Telephony.Sms.Inbox.DEFAULT_SORT_ORDER;
    }
}

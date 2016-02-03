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
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmlon on 2/1/2016.
 */
public class SmsGetWayServer extends NanoHTTPD {
    private static final String SMS_CONTENT_URI = "content://sms/conversations/";
    private static final String SMS_CONTENT_INBOX = "content://sms/inbox";
    private static final String SMS_CONTENT_SENT = "content://sms/sent";
    private static String SENT = "SMS_SENT";
    private static final String SENT_SMS_BUNDLE = "sent";
    private static String DELIVERED = "SMS_DELIVERED";
    private static final String DELIVERED_SMS_BUNDLE = "delivered";

    SmsManager smgr;
    TextView tv_serverLog;
    String str_log;
    Context mContext;

    private  BroadcastReceiver sentMessageBroadcastReceiver = new BroadcastReceiver() {
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
                    delSmsFromInbox(to, message);
                    Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(context, "SMS not delivered", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

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
                .query(SmsQuery.SENT_CONTENT_URI, SmsQuery.PROJECTION, sb.toString(), null,
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
        Cursor c = mContext.getContentResolver().query(SmsQuery.SENT_CONTENT_URI, null, sb.toString(), null,
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

    private  void deleteSMS(String to, String message){

    }

    private void sendSMS(String to, String message){
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

    public SmsGetWayServer(String hostname, int port, TextView tv_serverLog, Context context) {
        super(hostname, port);
        this.mContext = context;
        this.smgr = SmsManager.getDefault();
        this.tv_serverLog = tv_serverLog;
        this.mContext.registerReceiver(sentMessageBroadcastReceiver, new IntentFilter(SENT));
        this.mContext.registerReceiver(deliveredMessageBroadcastReceiver, new IntentFilter(DELIVERED));
    }

    @Override
    public Response serve(IHTTPSession session) throws IOException, ResponseException {
        HashMap<String, String> map;
        map = new HashMap<String, String>();
        session.parseBody(map);
        Map<String, String> params = session.getParms();
        String sendTo = params.get("To");
        String sendMsg = params.get("Message");
        if (sendTo != null && sendMsg != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                this.sendSMS(sendTo, sendMsg);
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{'success':'true'}");
            } catch (Exception ex) {
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{'success':'false'}");
            }
        }
        else {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{'success':'false'}");
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

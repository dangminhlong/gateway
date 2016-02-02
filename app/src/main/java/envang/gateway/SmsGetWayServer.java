package envang.gateway;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmlon on 2/1/2016.
 */
public class SmsGetWayServer extends NanoHTTPD {
    final String SENT = "SMS_SENT";
    final String DELIVERED = "SMS_DELIVERED";

    PendingIntent sentPI;
    PendingIntent deliveredPI;

    SmsManager smgr;
    TextView tv_serverLog;
    String str_log;

    public SmsGetWayServer(String hostname, int port, TextView tv_serverLog, Context context) {
        super(hostname, port);
        this.smgr = SmsManager.getDefault();
        this.tv_serverLog = tv_serverLog;
        sentPI = PendingIntent.getBroadcast(context, 0, new Intent(SENT), 0);
        deliveredPI = PendingIntent.getBroadcast(context, 0, new Intent(DELIVERED), 0);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS sent", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        }, new IntentFilter(SENT));
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                switch (getResultCode()){
                    case Activity.RESULT_OK:
                        Toast.makeText(context, "SMS delivered", Toast.LENGTH_SHORT).show();

                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
            }
        }, new IntentFilter(DELIVERED));
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
                smgr.sendTextMessage(sendTo, null, sendMsg, sentPI, deliveredPI);
                str_log = "Success:" + sendTo + "@" + sendMsg;
                tv_serverLog.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_serverLog.append(str_log + "\n");
                    }
                });
                try {
                    jsonObject.put("success", "true");
                } catch (Exception ec) {
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{'success':'true'}");
                }
            } catch (Exception ex) {
                str_log = "Failed:" + sendTo + "@" + sendMsg;
                tv_serverLog.post(new Runnable() {
                    @Override
                    public void run() {
                        tv_serverLog.append(str_log + "\n");
                    }
                });
                try {
                    jsonObject.put("success", "false");
                } catch (Exception e) {
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{'success':'false'}");
                }
            }
            String msg = jsonObject.toString();
            Response response = newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", msg);
            return response;
        }
        else {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{success:false}");
        }
    }
}

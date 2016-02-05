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
    MainActivity.SmsHandler smsHandler;

    public SmsGetWayServer(String hostname, int port, TextView tv_serverLog, MainActivity.SmsHandler smsHandler) {
        super(hostname, port);
        this.smsHandler = smsHandler;
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
            try {
                this.smsHandler.sendSMS(sendTo, sendMsg);
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{\"success\":\"true\"}");
            } catch (Exception ex) {
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{\"success\":\"false\"}");
            }
        }
        else {
            return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{\"success\":\"false\"}");
        }
    }


}

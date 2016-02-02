package envang.gateway;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.telephony.SmsManager;
import android.widget.TextView;

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

    SmsManager smgr;
    TextView tv_serverLog;
    String str_log;

    public SmsGetWayServer(String hostname, int port, TextView tv_serverLog) {
        super(hostname, port);
        this.smgr = SmsManager.getDefault();
        this.tv_serverLog = tv_serverLog;
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
                smgr.sendTextMessage(sendTo, null, sendMsg, null, null);
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

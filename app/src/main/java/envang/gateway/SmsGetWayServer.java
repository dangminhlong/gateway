package envang.gateway;

import android.telephony.SmsManager;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dmlon on 2/1/2016.
 */
public class SmsGetWayServer extends NanoHTTPD {

    SmsManager smgr;
    TextView tv_serverLog;

    public SmsGetWayServer(String hostname, int port, TextView tv_serverLog) {
        super(hostname, port);
        this.smgr = SmsManager.getDefault();
        this.tv_serverLog = tv_serverLog;
    }

    @Override
    public Response serve(IHTTPSession session){
        HashMap<String, String> map = new HashMap<String, String>();

        Map<String, String> params = session.getParms();
        String sendTo = params.get("To");
        String sendMsg = params.get("Message");
        if (sendTo != null && sendMsg != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                smgr.sendTextMessage(sendTo, null, sendMsg, null, null);
                try {
                    tv_serverLog.append("Success:" + sendTo + "@" + sendMsg);
                    jsonObject.put("sucess", "true");
                } catch (Exception ec) {
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{success:true}");
                }
            } catch (Exception ex) {
                tv_serverLog.append("Failed:" + sendTo + "@" + sendMsg);
                try {
                    jsonObject.put("sucess", "false");
                } catch (Exception e) {
                    return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{success:false}");
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

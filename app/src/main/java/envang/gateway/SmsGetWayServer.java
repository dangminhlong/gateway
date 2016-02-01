package envang.gateway;

import android.telephony.SmsManager;

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

    public SmsGetWayServer(String hostname, int port) {
        super(hostname, port);
        this.smgr = SmsManager.getDefault();
    }

    @Override
    public Response serve(IHTTPSession session){
        HashMap<String, String> map = new HashMap<String, String>();

        Map<String, String> params = session.getParms();
        String sendTo = params.get("To");
        String sendMsg = params.get("Message");
        JSONObject jsonObject = new JSONObject();
        try {
            smgr.sendTextMessage(sendTo, null, sendMsg, null, null);
            try {
                jsonObject.put("sucess", "true");
            }
            catch (Exception ec){
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", "{success:true}");
            }
        }
        catch (Exception ex) {
            try {
                jsonObject.put("sucess", "false");
            } catch (Exception e) {
                return newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json","{success:false}");
            }
        }
        String msg = jsonObject.toString();
        Response response = newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json", msg);
        return response;
    }
}

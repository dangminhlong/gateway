package envang.gateway;

import android.telephony.SmsManager;

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
        Map<String, String> params = session.getParms();
        String sendTo = params.get("To");
        String sendMsg = params.get("Message");
        try {
            smgr.sendTextMessage(sendTo, null, sendMsg, null, null);
            return newFixedLengthResponse("{success:true}");
        }
        catch (Exception ex){
            return newFixedLengthResponse("{success:false}");
        }

    }
}

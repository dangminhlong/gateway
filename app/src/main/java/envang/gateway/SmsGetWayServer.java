package envang.gateway;

import java.util.Map;

/**
 * Created by dmlon on 2/1/2016.
 */
public class SmsGetWayServer extends NanoHTTPD {
    public SmsGetWayServer(String hostname, int port) {
        super(hostname, port);
    }

    @Override
    public Response serve(IHTTPSession session){
        Map<String, String> parms = session.getParms();
        return newFixedLengthResponse("{success:true}");
    }
}

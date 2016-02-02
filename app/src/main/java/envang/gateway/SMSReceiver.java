package envang.gateway;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

/**
 * Created by dmlon on 2/3/2016.
 */
public class SMSReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            return;
        }
    }

    private SmsMessage[] getMessagesFromIntent(Intent intent) {
        SmsMessage retMsgs[] = null;
        Bundle bdl = intent.getExtras();
        try {
            Object pdus[] = (Object[]) bdl.get("pdus");
            retMsgs = new SmsMessage[pdus.length];
            for (int n = 0; n < pdus.length; n++) {
                byte[] byteData = (byte[]) pdus[n];
                retMsgs[n] = SmsMessage.createFromPdu(byteData);
            }

        } catch (Exception e) {
            Log.e("KALSMS", "GetMessages ERROR\n" + e);
        }
        return retMsgs;
    }

    private void DeleteSMSFromInbox(Context context, SmsMessage mesg) {
        Log.d("KALSMS", "try to delete SMS");

        try {
            Uri uriSms = Uri.parse("content://sms/inbox");

            StringBuilder sb = new StringBuilder();
            sb.append("address='" + mesg.getOriginatingAddress() + "' AND ");
            sb.append("body='" + mesg.getMessageBody() + "'");
            Cursor c = context.getContentResolver().query(uriSms, null, sb.toString(), null, null);
            c.moveToFirst();
            int thread_id = c.getInt(1);
            context.getContentResolver().delete(Uri.parse("content://sms/conversations/" + thread_id), null, null);
            c.close();
        } catch (Exception ex) {
            // deletions don't work most of the time since the timing of the
            // receipt and saving to the inbox
            // makes it difficult to match up perfectly. the SMS might not be in
            // the inbox yet when this receiver triggers!
            Log.d("SmsReceiver", "Error deleting sms from inbox: " + ex.getMessage());
        }
    }
}

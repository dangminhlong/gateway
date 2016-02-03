package envang.gateway;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

/**
 * @author Ushahidi Team <team@ushahidi.com>
 */
public class Util {

    private static final int KITKAT = 19;

    private Util() {
        // No instance
    }

    /**
     * Gets the current users phone number
     *
     * @param context is the context of the activity or service
     * @return a string of the phone number on the device
     */
    public static String getMyPhoneNumber(Context context) {
        TelephonyManager mTelephonyMgr;
        mTelephonyMgr = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        return mTelephonyMgr.getLine1Number();
    }

    /**
     * Check if the device runs Android 4.4 (KitKat) or higher.
     */
    public static boolean isKitKatOrHigher() {
        return Build.VERSION.SDK_INT >= KITKAT;
    }
}
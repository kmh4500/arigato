package de.schildbach.wallet.util;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

/**
 * Created by 민현 on 14. 3. 25.
 */
public class PhoneUtil {

    private static String simCountryCode;

    public static void init(Context context) {
        //  the Telephony Manager
        TelephonyManager telephonyManager=(TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        // Access Sim Country Code
        simCountryCode = telephonyManager.getSimCountryIso().toUpperCase();
        System.out.println("sim country : " + simCountryCode);
    }

    public static String normalize(String phone) throws NumberParseException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneProto = phoneUtil.parse(phone, simCountryCode);
        String normalized = phoneUtil.format(phoneProto, PhoneNumberUtil.PhoneNumberFormat.E164);
        if (normalized.startsWith("+")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}

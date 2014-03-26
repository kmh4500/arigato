package de.schildbach.wallet.util;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

/**
 * Created by 민현 on 14. 3. 25.
 */
public class PhoneUtil {

    public static String normalize(String phone) throws NumberParseException {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber phoneProto = phoneUtil.parse(phone, Locale.getDefault().getCountry());
        String normalized = phoneUtil.format(phoneProto, PhoneNumberUtil.PhoneNumberFormat.E164);
        if (normalized.startsWith("+")) {
            normalized = normalized.substring(1);
        }
        return normalized;
    }
}

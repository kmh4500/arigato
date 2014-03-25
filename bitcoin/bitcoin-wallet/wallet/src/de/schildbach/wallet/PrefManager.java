package de.schildbach.wallet;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by 민현 on 14. 3. 25.
 */
public class PrefManager {
    private SharedPreferences mPref;
    private static PrefManager mInstance;
    private static String PHONE_REGISTERED = "phone_registered";
    private final String CONTACTS_COUNT = "contacts_count";

    public PrefManager(Context context) {
        mPref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static void init(Context context) {
        mInstance = new PrefManager(context);
    }

    public static PrefManager getInstance() {
        return mInstance;
    }

    public boolean isPhoneRegistered() {
        return mPref.getBoolean(PHONE_REGISTERED, false);
    }

    public void setPhoneRegistered(boolean value) {
        mPref.edit().putBoolean(PHONE_REGISTERED, value).commit();
    }

    public int getContactCount() {
        return mPref.getInt(CONTACTS_COUNT, 0);
    }

    public void putContactCount(int count) {
        mPref.edit().putInt(CONTACTS_COUNT, count).commit();
    }
}

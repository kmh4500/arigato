package de.schildbach.wallet.service;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

import de.schildbach.wallet.AddressBookProvider;
import de.schildbach.wallet.PrefManager;
import de.schildbach.wallet.WalletApplication;
import de.schildbach.wallet.util.PhoneUtil;

/**
 * Created by kmh on 14. 3. 23.
 */
public class ContactsAddService extends IntentService {

    private static boolean firstRun = true;

    /**
         * A constructor is required, and must call the super IntentService(String)
         * constructor with a name for the worker thread.
         */
        public ContactsAddService() {
            super("ContactsAddService");
        }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
         * The IntentService calls this method from the default worker thread with
         * the intent that started the service. When this method returns, IntentService
         * stops the service, as appropriate.
         */
        @Override
        protected void onHandleIntent(Intent intent) {
            if(firstRun || hasNewContacts()) {
                firstRun = false;
                JSONArray array = new JSONArray();

                Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,	null, null, null, null);

                while (cursor.moveToNext()) {
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    try {
                        array.put(PhoneUtil.normalize(phone));
                    } catch (NumberParseException e) {
                        e.printStackTrace();
                    }
                }
                Log.d("HS", array.toString());
                try {
                    String response = new RestClient("/get").AddParam("pn", array.toString()).execute(RequestMethod.POST);
                    if (!TextUtils.isEmpty(response)) {
                       JSONObject object = new JSONObject(response);
                        if (object.getInt("err") == 0) {
                            JSONArray numbers = object.getJSONArray("phone_numbers");
                            for (int i = 0; i < numbers.length(); ++i) {
                                JSONObject item = numbers.getJSONObject(i);

                                final ContentValues values = new ContentValues();
                                String phone = item.getString("phone_number");
                                String address = item.getString("public_key");
                                values.put(AddressBookProvider.KEY_LABEL, getName(phone));
                                values.put(AddressBookProvider.KEY_PHONE, phone);
                                values.put(AddressBookProvider.KEY_ADDRESS, address);
                                final Uri uri = AddressBookProvider.contentUri(getPackageName()).buildUpon().appendPath(address).build();
                                getContentResolver().insert(uri, values);
                            }
                        }
                }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    public String getName(String phone) {
        String selection = ContactsContract.CommonDataKinds.Phone.NUMBER + " = " + phone;
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,	null, selection, null, null);

        if (cursor.moveToNext()) {
            return cursor.getString(cursor.getColumnIndex(
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
        }
        return "";
    }

    public boolean hasNewContacts() {
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,	null, null, null, null);
        int count = cursor.getCount();
        int oldCount = PrefManager.getInstance().getContactCount();
        if (oldCount == count) {
            return false;
        }
        PrefManager.getInstance().putContactCount(count);
        return true;
    }
}

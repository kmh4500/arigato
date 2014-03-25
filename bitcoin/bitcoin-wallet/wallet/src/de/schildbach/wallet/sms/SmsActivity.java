package de.schildbach.wallet.sms;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

import de.schildbach.wallet.service.RequestMethod;
import de.schildbach.wallet.service.RestClient;
import de.schildbach.wallet.util.PhoneUtil;
import de.schildbach.wallet_test.R;

public class SmsActivity extends Activity {
    String publicKey;
    private String mPhoneNumber;
    private SmsReceiver mSmsReciver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_verification);

        createBitcoinAccount();
        mPhoneNumber = getMyPhoneNumber();
        init(mPhoneNumber);


        EditText verificationCodeEditText = (EditText) findViewById(R.id.verificationCodeEditText);
        final String verificationKey = (verificationCodeEditText.getText()).toString();

        Button verifyButton = (Button) this.findViewById(R.id.verifyButton);
        verifyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mSmsReciver.verify(mPhoneNumber, verificationKey, publicKey);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSmsReciver = new SmsReceiver();
        mSmsReciver.init(publicKey);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsReceiver.SMS_RECEIVED);
        registerReceiver(mSmsReciver, intentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mSmsReciver);
        super.onPause();
    }

    void createBitcoinAccount() {
        publicKey = "shk_public_key";
    }

    void init(String phoneNumber) {
        EditText phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
        phoneNumberEditText.setText(phoneNumber);

        //if(phoneNumber.length()>=12)
        //new HtmlClient().execute("http://arigato-bitcoin.appspot.com/init?pn="+phoneNumber);

        Button smsButton = (Button) findViewById(R.id.smsButton);
        smsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
                String phoneNumber = (phoneNumberEditText.getText()).toString();
                new RestClient("init?pn=" + phoneNumber).executeInBackground(RequestMethod.GET);
            }
        });
    }

    String getMyPhoneNumber() {
        TelephonyManager phoneManager = (TelephonyManager)
                getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String number = null;
        try {
            number = PhoneUtil.normalize(phoneManager.getLine1Number());
            return number;
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}


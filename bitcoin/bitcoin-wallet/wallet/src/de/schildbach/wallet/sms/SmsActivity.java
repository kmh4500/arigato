package de.schildbach.wallet.sms;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.Wallet;
import com.google.i18n.phonenumbers.NumberParseException;

import de.schildbach.wallet.Constants;
import de.schildbach.wallet.PrefManager;
import de.schildbach.wallet.WalletApplication;
import de.schildbach.wallet.service.RequestMethod;
import de.schildbach.wallet.service.RestClient;
import de.schildbach.wallet.util.PhoneUtil;
import de.schildbach.wallet.util.WalletUtils;
import de.schildbach.wallet_test.R;

public class SmsActivity extends Activity {
    private SmsReceiver mSmsReciver;
    private RestClient.ResultCallback verificationCallback = new RestClient.ResultCallback() {

        @Override
        public void onResult(final RestClient.ErrorCode errorCode) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SmsActivity.this, errorCode.getErrorStringResId(), Toast.LENGTH_LONG).show();
                }
            });
            if (errorCode == RestClient.ErrorCode.SUCCESS) {
                PrefManager.getInstance().setPhoneRegistered(true);
                finish();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_verification);

        init(getMyPhoneNumber());

        Button verifyButton = (Button) this.findViewById(R.id.verifyButton);
        verifyButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                EditText verificationCodeEditText = (EditText) findViewById(R.id.verificationCodeEditText);
                final String verificationKey = (verificationCodeEditText.getText()).toString();
                mSmsReciver.verify(getPhoneNumberInEditText(), verificationKey, getAddressStr());
            }
        });
    }
    
    private String getAddressStr() {
        WalletApplication application = (WalletApplication) getApplication();
        Address address = WalletUtils.pickOldestKey(application.getWallet()).toAddress(Constants.NETWORK_PARAMETERS);
        return address.toString();
    }

    @Override
    protected void onResume() {
        super.onResume();

        mSmsReciver = new SmsReceiver();
        mSmsReciver.init(getAddressStr(), verificationCallback);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SmsReceiver.SMS_RECEIVED);
        registerReceiver(mSmsReciver, intentFilter);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(mSmsReciver);
        super.onPause();
    }

    void init(String phoneNumber) {
        EditText phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
        phoneNumberEditText.setText(phoneNumber);

        Button smsButton = (Button) findViewById(R.id.smsButton);
        smsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                new RestClient("init?pn=" + getPhoneNumberInEditText()).executeInBackground(RequestMethod.GET, null);
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

    public String getPhoneNumberInEditText() {
        EditText phoneNumberEditText = (EditText) findViewById(R.id.phoneNumberEditText);
        String phoneNumber = (phoneNumberEditText.getText()).toString();
        try {
            String normalized = PhoneUtil.normalize(phoneNumber);
            phoneNumberEditText.setText(normalized);
            return normalized;
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}


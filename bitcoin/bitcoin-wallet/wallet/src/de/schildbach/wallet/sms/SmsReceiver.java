package de.schildbach.wallet.sms;

import android.widget.EditText;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import de.schildbach.wallet.service.RequestMethod;
import de.schildbach.wallet.service.RestClient;
import de.schildbach.wallet_test.R;

public class SmsReceiver extends BroadcastReceiver {
    String publicKey;
    Context context;
    public static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

    @Override public void onReceive(Context context, Intent intent){
        this.context=context;
//Log.v("sms", "SMS received");
        if(intent.getAction().equals(SMS_RECEIVED)){
            Bundle bundle = intent.getExtras();
            if(bundle==null){
                return;
            }
            Object[] pdusObj = (Object[]) bundle.get("pdus");
            if(pdusObj==null){
                return;
            }
            String MessageBody = "";
            SmsMessage[] smsMessages = new SmsMessage[pdusObj.length];
            for(int i=0; i<pdusObj.length; i++){
                smsMessages[i] = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
                MessageBody+=smsMessages[i].getMessageBody();
            }
            //Log.v("sms", "SMS received: <"+MessageBody+">");
            String verificationKey=MessageBody;
            if(verificationKey.length()>6)
                verificationKey=verificationKey.substring(0, 6);
            EditText verificationCodeEditText=(EditText)((SmsActivity)context).findViewById(R.id.verificationCodeEditText);
            verificationCodeEditText.setText(verificationKey);
            EditText phoneNumberText=(EditText)((SmsActivity)context).findViewById(R.id.phoneNumberEditText);
            verify(phoneNumberText.toString(), verificationKey, publicKey);
        }
    }

    void init(String publicKey){
        this.publicKey=publicKey;
    }

    void verify(String phoneNumber, String verificationCode, String publicKey){
        try {
            new RestClient("verify?pn="+phoneNumber+"&vc="+verificationCode+"&pk="+publicKey).executeInBackground(RequestMethod.GET);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

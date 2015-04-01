package com.ramyasaketha.smsenhancerapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class EnhancerAppReceiver extends BroadcastReceiver {

    private static final String SMS_EXTRA_NAME = "pdus";
    private static final String TAG = "EnhancerAppReceiver";

    public EnhancerAppReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceiver");
        Bundle extras = intent.getExtras();
        String message = "";
        if (extras != null) {
            Object[] smsData = (Object[]) extras.get(SMS_EXTRA_NAME);
            for (int i = 0; i < smsData.length; i++) {
                SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsData[i]);
                String body = sms.getMessageBody();
                String address = sms.getDisplayOriginatingAddress();
                message += "SMS from" + address + "\n";
                message += body + "\n";
            }
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
            launchActivity(context);
        }
    }

    public void launchActivity(Context context) {
        Intent intent = new Intent();
        intent.setClassName("com.ramyasaketha.smsenhancerapp", "com.ramyasaketha.smsenhancerapp.ReceivedSmsListActivity");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}

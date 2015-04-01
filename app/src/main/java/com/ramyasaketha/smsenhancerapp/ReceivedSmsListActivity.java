package com.ramyasaketha.smsenhancerapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;


public class ReceivedSmsListActivity extends Activity implements AdapterView.OnItemClickListener, TextToSpeech.OnInitListener{

    private static final String TAG = "ReceivedSmsListActivity";
    private static final int MY_DATA_CHECK_CODE = 135;
    private TextToSpeech mTts;
    ArrayList<String> mSmsList = new ArrayList<String>();
    private UpdateList mAsyncTask = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_received_sms_list);

        Intent intent = new Intent();
        intent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, MY_DATA_CHECK_CODE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, " onDestroy Called in mAsyncTask");
        // Cancel AsyncTask.
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
            mAsyncTask = null;
        }
        if (mTts != null) {
            mTts.shutdown();
            mTts.stop();
            mTts = null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_received_sms_list, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemClick");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                mTts = new TextToSpeech(this, this);
                mAsyncTask = new UpdateList();
                mAsyncTask.execute();
            } else {
                Intent installIntent = new Intent();
                installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
        }
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            mTts.speak("New Message Received", TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private class UpdateList extends AsyncTask<Void, Void, Void> {
        private ProgressDialog dialog;

        @Override
        protected Void doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");
            UpdateSmsListToView();
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "onPreExecute");
            dialog = new ProgressDialog(ReceivedSmsListActivity.this);
            dialog.setTitle("Please Wait...");
            dialog.setMessage("Loading SetupItems");
            dialog.setCancelable(false);
            dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Log.d(TAG, "onPostExecute");
            ListView smsListView = (ListView) findViewById(R.id.SMSList);
            smsListView.setAdapter(new ArrayAdapter(ReceivedSmsListActivity.this, android.R.layout.simple_list_item_1, mSmsList));
            smsListView.setOnItemClickListener(ReceivedSmsListActivity.this);
            dialog.dismiss();
        }
    }

    private void UpdateSmsListToView() {
        Log.d(TAG, " UpdateSmsListToView");
        ContentResolver contResolver = getContentResolver();
        Cursor curs = contResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);

        int indexBody = curs.getColumnIndex("body");
        int indexAddress = curs.getColumnIndex("address");

        if (indexBody < 0 || !curs.moveToFirst()) {
            Log.d(TAG, "No Message");
            return;
        }

        do {
            String mNewMessage = "Sender...: " + curs.getString(indexAddress) + "\n"
                    + curs.getString(indexBody);
            Log.d(TAG, "Message: " + mNewMessage);
            mSmsList.add(mNewMessage);
        } while (curs.moveToNext());
        curs.close();
        // Get last message info
        //curs.moveToFirst();
    }
}

package com.bufferinmuffins.bookinator;

import android.graphics.Color;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;


public class ChangePwdActivity extends ActionBarActivity {

    protected String mTitle = "Change Password";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);
    }


    public void onClick(final View view) {

        if (view.getId() == R.id.change_pwd_submit) {
            if (((EditText)findViewById(R.id.change_pwd_new)).getText().toString().length() < 7) {

                ((TextView)findViewById(R.id.change_pwd_errmsg)).setText("Password must be at least 7 characters long.");
                return;
            }
            if (!(((EditText)findViewById(R.id.change_pwd_new)).getText().toString().equals(((EditText)findViewById(R.id.change_pwd_confirm)).getText().toString()))) {
                ((TextView)findViewById(R.id.change_pwd_errmsg)).setText("Passwords are not the same.");
                return;
            }
            new ChangePwdTask().execute(((EditText)findViewById(R.id.change_pwd_new)).getText().toString());
        }

    }

    public void onTaskResponse(Boolean pass) {
        if (!pass) {
            ((TextView)findViewById(R.id.change_pwd_errmsg)).setText("Unexpected error occurred.");
            return;
        }
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_change_pwd, menu);
        return true;
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

    private class ChangePwdTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient cli = new DefaultHttpClient();
            HttpPut putReq;

            try {

                putReq = new HttpPut(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/accounts?apiKey=" + getString(R.string.mongolab_apikey) + "&q=" + URLEncoder.encode("{\"email\":\"" + LoginActivity.bsession.getEmail() + "\"}", "UTF-8")));
            } catch (Exception e) {

                e.printStackTrace();
                return false;
            }
            HttpResponse postResp;
            String result;
            JSONObject jop = new JSONObject();
            putReq.addHeader("Content-Type", "application/json");

            try {
                jop.put("pwd", BookinatorSession.getSHA256(params[0]));
                jop.put("email", LoginActivity.bsession.getEmail());
                jop.put("name", LoginActivity.bsession.getName());

                putReq.setEntity(new StringEntity(jop.toString(), "UTF8"));
                postResp = cli.execute(putReq);
                result = new BasicResponseHandler().handleResponse(postResp);

            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            cli.getConnectionManager().shutdown();
            if (result.length() < 10) {
                return false;
            }


            return true;
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            onTaskResponse(aBoolean);
        }
    }
}

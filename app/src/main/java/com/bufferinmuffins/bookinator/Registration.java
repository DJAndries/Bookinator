package com.bufferinmuffins.bookinator;

import android.graphics.Color;
import android.os.AsyncTask;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;

/**
 * Created by student327 on 29/01/2015.
 */
public class Registration {
    private EditText nameField;
    private EditText emailField;
    private EditText pwdField;
    private EditText cpwdField;
    private TextView errMsgView;
    private String apiKey;
    private RegisterActivity regActivity;
    private String errMsg;

    public String getErrMsg() {
        return errMsg;
    }

    public Registration(EditText nameField, EditText emailField, EditText pwdField, EditText cpwdField, TextView errMsgView, String apiKey, RegisterActivity regActivity) {
        this.nameField = nameField;
        this.emailField = emailField;
        this.pwdField = pwdField;
        this.cpwdField = cpwdField;
        this.errMsgView = errMsgView;
        this.apiKey = apiKey;
        this.regActivity = regActivity;
        init();
    }

    private void init() {
        nameField.setTextColor(Color.BLACK);
        emailField.setTextColor(Color.BLACK);
        pwdField.setTextColor(Color.BLACK);
        cpwdField.setTextColor(Color.BLACK);
        errMsgView.setText("");
    }

    private String getSHA256(String pwd) {
        byte[] out;
        String out2 = "";
        try {
            java.security.MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(pwd.getBytes());
            out = md.digest();
        } catch(Exception e) {
            return null;
        }
        for (int i = 0; i < 32; i++) {
            String hex = Integer.toHexString(0xFF & out[i]);
            if (hex.length() == 1) {
                hex = "0" + hex;
            }
            out2 += hex;
        }
        return out2;
    }

    public boolean validate() {
        init();
        if (nameField.getText().toString().length() < 3) {
            nameField.setTextColor(Color.RED);
            errMsgView.setText("Name must be provided.");
            return false;
        }
        String emailText = emailField.getText().toString();
        int atIndex = emailText.indexOf("@");
        if (atIndex < 1 || (emailText.substring(atIndex).equalsIgnoreCase("@bcit.ca") && emailText.substring(atIndex).equalsIgnoreCase("@my.bcit.ca"))) {
            emailField.setTextColor(Color.RED);
            errMsgView.setText("Valid BCIT email must be provided.");
            return false;
        }

        if (pwdField.getText().toString().length() < 7) {
            pwdField.setTextColor(Color.RED);
            errMsgView.setText("Password must be at least 7 characters long.");
            return false;
        }

        if (!pwdField.getText().toString().equals(cpwdField.getText().toString())) {
            pwdField.setTextColor(Color.RED);
            cpwdField.setTextColor(Color.RED);
            errMsgView.setText("Passwords must be equal to each other.");
            return false;
        }

        return true;
    }
    public void register() {
        new RegisterTask().execute(emailField.getText().toString(), pwdField.getText().toString(), nameField.getText().toString());
    }

    private class RegisterTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient cli = new DefaultHttpClient();
            HttpGet getReq;

            try {
                getReq = new HttpGet(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/accounts?apiKey="
                        + apiKey + "&q=" + URLEncoder.encode("{\"email\":\"" + params[0] + "\"}", "UTF-8")));
            } catch (Exception e) {
                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }
            HttpResponse getResp;
            String result;

            getReq.addHeader("Content-Type", "application/json");
            try {

                getResp = cli.execute(getReq);
                result = new BasicResponseHandler().handleResponse(getResp);
            } catch (Exception e) {
                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }
            cli.getConnectionManager().shutdown();
            if (result.length() > 6) {
                errMsg = "Email already registered in system.";
                return false;
            }


            //yo


            cli = new DefaultHttpClient();
            HttpPost postReq;

            try {

                postReq = new HttpPost(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/accounts?apiKey=" + apiKey));
            } catch (Exception e) {
                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }
            HttpResponse postResp;

            JSONObject jop = new JSONObject();
            postReq.addHeader("Content-Type", "application/json");

            try {
                jop.put("email", params[0]);
                jop.put("pwd", getSHA256(params[1]));
                jop.put("name", params[2]);
                postReq.setEntity(new StringEntity(jop.toString(), "UTF8"));
                postResp = cli.execute(postReq);
                result = new BasicResponseHandler().handleResponse(postResp);
            } catch (Exception e) {
                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }
            cli.getConnectionManager().shutdown();
            if (result.length() < 10) {
                errMsg = "Unexpected error occurred. Please try again.";
                return false;
            }
            return true;
        }


        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            regActivity.onRegisterResponse(aBoolean);
        }
    }
}

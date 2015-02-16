package com.bufferinmuffins.bookinator;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.net.URI;
import java.net.URLEncoder;
import java.security.MessageDigest;

public class BookinatorSession implements Serializable {

    private String userid;
    private String email;
    private String name;
    private String sessid;
    private String apiKey;
    private String errMsg = "";

    public String getSessID() {
        return sessid;
    }

    public LoginActivity getLoginActivity() {
        return loginActivity;
    }

    public void setLoginActivity(LoginActivity loginActivity) {
        this.loginActivity = loginActivity;
    }

    private LoginActivity loginActivity;

    public BookinatorSession(String apiKey, LoginActivity act) {
        this.apiKey = apiKey;
        this.loginActivity = act;
    }

    public BookinatorSession(String email, String sessid, String apiKey, LoginActivity act) {
        this.apiKey = apiKey;
        this.email = email;
        this.sessid = sessid;
        this.loginActivity = act;
    }


    public String getName() {
        return name;
    }
    public String getErrMsg() { return errMsg; }

    public void login(String email, String pwd) {
        new LoginTask().execute(email, pwd);
    }
    public void checkSession(String sessid) {
        this.sessid = sessid;
        new SessionCheckTask().execute();
    }
    public void closeSession() {
        new SessionCloseTask().execute();
    }

    public String getEmail() {
        return email;
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

    public boolean isLoggedIn() {
        return false;
    }

    private class SessionCloseTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            if (sessid == "notagoodsession") {
                return false;
            }
            HttpClient cli = new DefaultHttpClient();
            HttpPut putReq;

            JSONObject jop = new JSONObject();
            JSONObject jop2 = new JSONObject();
            try {
                putReq = new HttpPut(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/sessions?apiKey="
                        + apiKey));
            } catch (Exception e) {

                Log.d("seesc", "1");
                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }
            HttpResponse putResp;
            String result;

            putReq.addHeader("Content-Type", "application/json");
            try {
                jop2.put("$oid", sessid);
                jop.put("_id", jop2);
                putReq.setEntity(new StringEntity(jop.toString(), "UTF8"));
                putResp = cli.execute(putReq);


            } catch (Exception e) {
                Log.d("seesc", "2");
                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }
            //login query
            cli.getConnectionManager().shutdown();

            name = "";
            email = "";
            sessid = "";
            userid = "";


            return true;
        }

    }

    private class SessionCheckTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            if (sessid == "notagoodsession") {
                return false;
            }
            HttpClient cli = new DefaultHttpClient();
            HttpGet getReq;

            //login query
            try {
                getReq = new HttpGet(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/sessions?apiKey="
                        + apiKey + "&q=" + URLEncoder.encode("{\"_id\":{\"$oid\":\"" + sessid + "\"}}", "UTF-8")));
            } catch (Exception e) {


                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }
            HttpResponse getResp;
            String result;
            Log.d("sessi", sessid);
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
            if (result.length() < 10) {
                errMsg = "Unexpected error occurred. Please try again";
                return false;
            }
            try {
                JSONArray jsarr = new JSONArray(result);
                name = jsarr.getJSONObject(0).getString("name");
                email = jsarr.getJSONObject(0).getString("email");
                userid = jsarr.getJSONObject(0).getJSONObject("_id").getString("$oid");
            } catch(Exception e) {

                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            loginActivity.onSessionResponse(aBoolean);
        }
    }

    private class LoginTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            HttpClient cli = new DefaultHttpClient();
            HttpGet getReq;

            //login query
            try {
                getReq = new HttpGet(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/accounts?apiKey="
                        + apiKey + "&q=" + URLEncoder.encode("{\"email\":\"" + params[0] + "\",\"pwd\":\"" + getSHA256(params[1]) + "\"}&fields={\"name\": 1, \"email\": 1, \"pwd\": 1}", "UTF-8")));
            } catch (Exception e) {
                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }
            HttpResponse getResp;
            String result;
            JSONObject jop1;

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
            if (result.length() < 10) {
                errMsg = "Incorrect email/password.";
                return false;
            }
            try {
                JSONArray jsarr = new JSONArray(result);
                name = jsarr.getJSONObject(0).getString("name");
                email = jsarr.getJSONObject(0).getString("email");
                userid = jsarr.getJSONObject(0).getJSONObject("_id").getString("$oid");
            } catch(Exception e) {
                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }

            //session store
            cli = new DefaultHttpClient();
            HttpPost postReq;

            try {

                postReq = new HttpPost(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/sessions?apiKey=" + apiKey));
            } catch (Exception e) {
                errMsg = "Unexpected error occurred. Please try again.";
                e.printStackTrace();
                return false;
            }
            HttpResponse postResp;

            JSONObject jop = new JSONObject();
            postReq.addHeader("Content-Type", "application/json");

            try {
                jop.put("userid", userid);
                jop.put("name", name);
                jop.put("email", email);
                postReq.setEntity(new StringEntity(jop.toString(), "UTF8"));
                postResp = cli.execute(postReq);
                result = new BasicResponseHandler().handleResponse(postResp);
                jop = new JSONObject(result);
                sessid = jop.getJSONObject("_id").getString("$oid");
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
            loginActivity.onLoginResponse(aBoolean);
        }
    }

}

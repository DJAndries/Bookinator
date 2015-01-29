package com.bufferinmuffins.bookinator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.client.ResponseHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.security.MessageDigest;

/**
 * Created by proctor on 1/28/2015.
 */
public class BookinatorSession {
    private String email;
    private String sessid;
    private String apiKey;

    public BookinatorSession(String apiKey) {
        this.apiKey = apiKey;
    }

    public BookinatorSession(String email, String sessid, String apiKey) {
        this.apiKey = apiKey;
        this.email = email;
        this.sessid = sessid;

    }

    public boolean login(String email, String pwd, String errMsg) {
        HttpClient cli = new DefaultHttpClient();
        HttpGet getReq = new HttpGet("https://api.mongolab.com/api/1/databases/bookinatordb/collections/accounts?apiKey="
            + apiKey + "&email={'email':'" + email + "'}");
        HttpResponse getResp;
        String result;
        getReq.addHeader("Content-Type", "application/json");
        try {

            getResp = cli.execute(getReq);
            result = new BasicResponseHandler().handleResponse(getResp);
        } catch (Exception e) {
            errMsg = "Unexpected error occurred. Please try again.";
            return false;
        }
        cli.getConnectionManager().shutdown();
        return true;
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
}

package com.bufferinmuffins.bookinator;

import android.graphics.Color;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by student327 on 29/01/2015.
 */
public class Registration {
    private EditText nameField;
    private EditText emailField;
    private EditText pwdField;
    private EditText cpwdField;
    private TextView errMsgView;

    public Registration(EditText nameField, EditText emailField, EditText pwdField, EditText cpwdField, TextView errMsgView) {
        this.nameField = nameField;
        this.emailField = emailField;
        this.pwdField = pwdField;
        this.cpwdField = cpwdField;
        this.errMsgView = errMsgView;
        init();
    }

    private void init() {

        errMsgView.setText("");
    }

    public boolean validate() {
        init();
        if (nameField.getText().toString().length() < 3) {
            errMsgView.setText("Name must be provided.");
            return false;
        }
        String emailText = emailField.getText().toString();
        int atIndex = emailText.indexOf("@");
        if (atIndex < 1 || (emailText.substring(atIndex) != "@bcit.ca" && emailText.substring(atIndex) != "@my.bcit.ca")) {
            errMsgView.setText("Valid BCIT email must be provided.");
            return false;
        }

        if (pwdField.getText().toString().length() < 7) {

            errMsgView.setText("Password must be at least 7 characters long.");
            return false;
        }

        if (!pwdField.getText().toString().equals(cpwdField.getText().toString())) {

            errMsgView.setText("Passwords must be equal to each other.");
            return false;
        }

        return true;
    }

}

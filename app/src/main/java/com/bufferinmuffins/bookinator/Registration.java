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
        initFieldColor();
    }

    private void initFieldColor() {
        nameField.setBackgroundColor(Color.WHITE);
        emailField.setBackgroundColor(Color.WHITE);
        pwdField.setBackgroundColor(Color.WHITE);
        cpwdField.setBackgroundColor(Color.WHITE);
        errMsgView.setText("");
    }

    public boolean validate() {
        initFieldColor();
        if (nameField.getText().toString().length() < 3) {
            nameField.setBackgroundColor(Color.parseColor("#F5768D"));
            errMsgView.setText("Name must be provided.");
            return false;
        }
        String emailText = emailField.getText().toString();
        int atIndex = emailText.indexOf("@");
        if (atIndex < 1 || (emailText.substring(atIndex) != "@bcit.ca" && emailText.substring(atIndex) != "@my.bcit.ca")) {
            emailField.setBackgroundColor(Color.parseColor("#F5768D"));
            errMsgView.setText("Valid BCIT email must be provided.");
            return false;
        }

        if (pwdField.getText().toString().length() < 7) {
            pwdField.setBackgroundColor(Color.parseColor("#F5768D"));
            errMsgView.setText("Password must be at least 7 characters long.");
            return false;
        }

        if (!pwdField.getText().toString().equals(cpwdField.getText().toString())) {
            pwdField.setBackgroundColor(Color.parseColor("#F5768D"));
            cpwdField.setBackgroundColor(Color.parseColor("#F5768D"));
            errMsgView.setText("Passwords must be equal to each other.");
            return false;
        }

        return true;
    }

}

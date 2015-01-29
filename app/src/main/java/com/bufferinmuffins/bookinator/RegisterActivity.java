package com.bufferinmuffins.bookinator;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class RegisterActivity extends ActionBarActivity {
    private Registration reg;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        reg = new Registration((EditText)findViewById(R.id.register_namefield), (EditText)findViewById(R.id.register_emailfield), (EditText)findViewById(R.id.register_passwordfield), (EditText)findViewById(R.id.register_cpwdfield), (TextView)findViewById(R.id.register_errmsgview));

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        return super.onOptionsItemSelected(item);
    }

    public void onClick(final View view) {
        reg.validate();
    }
}

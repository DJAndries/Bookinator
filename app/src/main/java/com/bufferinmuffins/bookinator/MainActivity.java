package com.bufferinmuffins.bookinator;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;

import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, HomeFragment.OnFragmentInteractionListener,
                    BookFragment.OnFragmentInteractionListener, AccountFragment.OnFragmentInteractionListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public static ArrayAdapter<String> currentInstructorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();
        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (position == 1) {
            new InstructorSpinnerTask().execute();
        } else if (position == 2) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, AccountFragment.newInstance())
                    .commit();

        }else if (position == 4) {
            LoginActivity.bsession.closeSession();
            SharedPreferences settings = getSharedPreferences("session", 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.remove("sessid");
            editor.commit();

            Intent i = new Intent(this, LoginActivity.class);

            startActivity(i);
            finish();
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, HomeFragment.newInstance())
                    .commit();
        }

    }

    public void onAccountClick(final View view) {
        if (view.getId() == R.id.account_changepwd) {
            Intent i = new Intent(getApplicationContext(), ChangePwdActivity.class);
            startActivity(i);

        }
    }

    public void onBookClick(final View view) {
        if (view.getId() == R.id.book_date_edit) {
            Calendar c = Calendar.getInstance();
            new DatePickerDialog(MainActivity.this, new BookDateSetListener((EditText)view), c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
        }
        if (view.getId() == R.id.book_time_edit) {
            Calendar c = Calendar.getInstance();
            new TimePickerDialog(MainActivity.this, new BookDateSetListener((EditText)view), c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false).show();
        }
    }

    public class BookDateSetListener implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
        private EditText field;
        public BookDateSetListener(EditText field) {
            this.field = field;
        }
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            field.setText((monthOfYear+1)+"-"+dayOfMonth+"-"+year);
        }

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            field.setText((hourOfDay == 0 ? 12 : (hourOfDay%13)+ (hourOfDay > 12 ? 1 : 0) )+":"+String.format("%02d", minute)+" "+(Math.floor((double)hourOfDay / 12) >= 1 ? "PM" : "AM"));
        }
    }
    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
            case 4:
                mTitle = getString(R.string.title_section4);
                break;
            case 5:
                mTitle = getString(R.string.title_section5);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    public void onHomeClick(final View view) {
        if (view == findViewById(R.id.home_book_button)) {
            mNavigationDrawerFragment.selectItem(1);
        } else if (view == findViewById(R.id.home_logout_button)) {
            mNavigationDrawerFragment.selectItem(4);
        } else if (view == findViewById(R.id.home_account_button)) {
            mNavigationDrawerFragment.selectItem(2);
        }
    }

    public class InstructorSpinnerTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            if (LoginActivity.bsession.getSessID() == "notagoodsession") {
                return false;
            }
            HttpClient cli = new DefaultHttpClient();
            HttpGet getReq;

            //login query
            try {
                getReq = new HttpGet(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/instructors?apiKey="
                        + getString(R.string.mongolab_apikey)));
            } catch (Exception e) {
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
                e.printStackTrace();
                return false;
            }
            cli.getConnectionManager().shutdown();
            if (result.length() < 10) {
                return false;
            }
            try {
                JSONArray jsarr = new JSONArray(result);
                ArrayList<String> instrArr = new ArrayList<String>();
                for (int i = 0; i < jsarr.length(); i++) {
                    instrArr.add(jsarr.getJSONObject(i).getString("name"));
                }
                currentInstructorList = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item, instrArr);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        public void onPostExecute(Boolean pass) {

            if (pass)
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, BookFragment.newInstance())
                        .commit();
        }

    }
    public class BookingTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            if (LoginActivity.bsession.getSessID() == "notagoodsession") {
                return false;
            }
            HttpClient cli = new DefaultHttpClient();
            HttpGet getReq;

            //login query
            try {
                getReq = new HttpGet(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/instructors?apiKey="
                        + getString(R.string.mongolab_apikey)));
            } catch (Exception e) {
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
                e.printStackTrace();
                return false;
            }
            cli.getConnectionManager().shutdown();
            if (result.length() < 10) {
                return false;
            }
            try {
                JSONArray jsarr = new JSONArray(result);
                ArrayList<String> instrArr = new ArrayList<String>();
                for (int i = 0; i < jsarr.length(); i++) {
                    instrArr.add(jsarr.getJSONObject(i).getString("name"));
                }
                currentInstructorList = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item, instrArr);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }
        public void onPostExecute(Boolean pass) {

            if (pass)
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, BookFragment.newInstance())
                        .commit();
        }

    }
}

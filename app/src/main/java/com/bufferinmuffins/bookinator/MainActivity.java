package com.bufferinmuffins.bookinator;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.app.AlarmManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, HomeFragment.OnFragmentInteractionListener,
                    BookFragment.OnFragmentInteractionListener, AccountFragment.OnFragmentInteractionListener, BookingsFragment.OnFragmentInteractionListener, SettingsFragment.OnFragmentInteractionListener, AdapterView.OnItemClickListener {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    public static ArrayAdapter<String> currentInstructorList;
    public static ArrayAdapter<String> currentBookingsList;
    public static ArrayList<String> instructorListIds;
    public static MainActivity currentInstance;

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
        as = new AlarmService(getApplicationContext());
        currentInstance = this;
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (position == 0) {
            new CheckBookingsTask().execute();
        } else if (position == 1) {
            new InstructorSpinnerTask().execute();
        } else if (position == 2) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, AccountFragment.newInstance())
                    .commit();
        } else if (position == 3) {
            fragmentManager.beginTransaction()
                    .replace(R.id.container, SettingsFragment.newInstance())
                    .commit();
        } else if (position == 4) {
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
        if (view.getId() == R.id.book_button_submit) {
            if (((EditText)findViewById(R.id.book_date_edit)).getText().toString().length() < 2 || ((EditText)findViewById(R.id.book_time_edit)).getText().toString().length() < 2) {
                Toast.makeText(getApplication(), "Please select a date and time", Toast.LENGTH_LONG);
                return;
            }
            new BookingTask().execute(((Spinner) findViewById(R.id.book_instructor_spinner)).getSelectedItemPosition() + "", ((EditText) findViewById(R.id.book_date_edit)).getText().toString(), ((EditText) findViewById(R.id.book_time_edit)).getText().toString());
        }
    }


    private int selectedBookingPos = -1;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        selectedBookingPos = position;
        final AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Confirmation");
        alertDialog.setMessage("Are you sure you wish to delete this appointment?");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "NO",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                new DeleteBookingTask().execute(selectedBookingPos);
            }
        });
        alertDialog.show();
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
        } else if (view == findViewById(R.id.home_check_button)) {
            mNavigationDrawerFragment.selectItem(0);
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
                instructorListIds = new ArrayList<String>();
                ArrayList<String> instrArr = new ArrayList<String>();
                for (int i = 0; i < jsarr.length(); i++) {
                    instrArr.add(jsarr.getJSONObject(i).getString("name"));
                    instructorListIds.add(jsarr.getJSONObject(i).getString("uid"));
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
    private AlarmService as;
    public class CheckBookingsTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            if (LoginActivity.bsession.getSessID() == "notagoodsession") {
                return false;
            }
            ArrayList<Date> ardate = new ArrayList<Date>();
            as.cancelAlarms();
            HttpClient cli = new DefaultHttpClient();
            HttpGet getReq;

            //login query
            try {
                getReq = new HttpGet(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/bookings?apiKey="
                        + getString(R.string.mongolab_apikey) + "&q=" + URLEncoder.encode("{\"" + (LoginActivity.bsession.getIsInstructor() ? "instructorid" : "userid") + "\":\"" + LoginActivity.bsession.getUserid() + "\"}", "UTF-8")));
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
                ArrayList<String> instrArr = new ArrayList<String>();
                currentBookingsList = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item, instrArr);
                return true;
            }

            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy hh:mm a");
            try {
                JSONArray jsarr = new JSONArray(result);
                ArrayList<String> instrArr = new ArrayList<String>();
                instructorListIds = new ArrayList<String>();
                SharedPreferences settings = MainActivity.currentInstance.getSharedPreferences("session", 0);
                for (int i = 0; i < jsarr.length(); i++) {
                    instrArr.add(jsarr.getJSONObject(i).getString(LoginActivity.bsession.getIsInstructor() ? "studentname" : "instructorname") + " " + jsarr.getJSONObject(i).getString("datetime"));
                    Date date1 = new Date();
                    instructorListIds.add(jsarr.getJSONObject(i).getJSONObject("_id").getString("$oid"));
                    if (formatter.parse(jsarr.getJSONObject(i).getString("datetime")).compareTo(date1) > 0 && settings.getBoolean("notifyEnable", true)) {
                        ardate.add(formatter.parse(jsarr.getJSONObject(i).getString("datetime")));
                    }
                }
                currentBookingsList = new ArrayAdapter<String>(getApplicationContext(),
                        android.R.layout.simple_spinner_dropdown_item, instrArr);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            as.startAlarm(ardate);
            return true;
        }
        public void onPostExecute(Boolean pass) {
            if (pass) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, BookingsFragment.newInstance())
                        .commit();

            }
        }

    }

    public class DeleteBookingTask extends AsyncTask<Integer, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Integer... params) {
            if (LoginActivity.bsession.getSessID() == "notagoodsession") {
                return false;
            }
            ArrayList<Date> ardate = new ArrayList<Date>();
            as.cancelAlarms();
            HttpClient cli = new DefaultHttpClient();
            HttpDelete getReq;

            //login query
            try {
                getReq = new HttpDelete(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/bookings/" + instructorListIds.get(params[0]) + "?apiKey="
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

            return true;
        }
        public void onPostExecute(Boolean pass) {
            mNavigationDrawerFragment.selectItem(0);
        }

    }


    public void postBookingTask() {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Congratulations!");
        alertDialog.setMessage("You have successfully booked an appointment. Please see the current appointments to check the status.");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mNavigationDrawerFragment.selectItem(0);
                    }
                });
        alertDialog.show();

    }

    public class BookingTask extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... params) {
            //session store
            HttpClient cli = new DefaultHttpClient();
            HttpPost postReq;

            try {

                postReq = new HttpPost(new URI("https://api.mongolab.com/api/1/databases/bookinatordb/collections/bookings?apiKey=" + getString(R.string.mongolab_apikey)));
            } catch (Exception e) {
                Toast.makeText(getApplication(), "Unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return false;
            }
            HttpResponse postResp;
            String result;

            JSONObject jop = new JSONObject();
            postReq.addHeader("Content-Type", "application/json");

            try {
                jop.put("userid", LoginActivity.bsession.getUserid());
                jop.put("instructorid", instructorListIds.get(Integer.parseInt(params[0])));
                jop.put("instructorname", currentInstructorList.getItem((Integer.parseInt(params[0]))));
                jop.put("studentname", LoginActivity.bsession.getName());
                jop.put("datetime", params[1] + " " + params[2]);
                postReq.setEntity(new StringEntity(jop.toString(), "UTF8"));
                postResp = cli.execute(postReq);
                result = new BasicResponseHandler().handleResponse(postResp);
                jop = new JSONObject(result);
            } catch (Exception e) {
                Toast.makeText(getApplication(), "Unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
                return false;
            }
            cli.getConnectionManager().shutdown();
            if (result.length() < 10) {
                Toast.makeText(getApplication(), "Unexpected error occurred. Please try again.", Toast.LENGTH_LONG).show();
                return false;
            }


            return true;
        }
        public void onPostExecute(Boolean pass) {

            if (pass) {
                postBookingTask();
            }
        }

    }
}

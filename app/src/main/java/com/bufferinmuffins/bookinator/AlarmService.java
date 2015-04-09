package com.bufferinmuffins.bookinator;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by student327 on 01/04/2015.
 */
public class AlarmService {
    private Context context;
    private PendingIntent mAlarmSender;
    private int alarmCount = 0;
    public AlarmService(Context context) {
        this.context = context;
        mAlarmSender = PendingIntent.getBroadcast(context, 0, new Intent(context, AlarmReceiver.class), 0);
    }

    public void startAlarm(ArrayList<Date> al){
        //Set the alarm to 10 seconds from now

        for (int i = 0; i < al.size(); i++) {
            /*Calendar c = Calendar.getInstance();
            c.set(Calendar.MONTH, al.get(i).getMonth());
            c.set(Calendar.DAY_OF_MONTH, al.get(i).getDate());
            c.set(Calendar.YEAR, al.get(i).getYear());
            c.set(Calendar.HOUR_OF_DAY, al.get(i).getHours());
            c.set(Calendar.MINUTE, al.get(i).getMinutes());
            c.set(Calendar.SECOND, al.get(i).getSeconds());
            long firstTime = c.getTimeInMillis();*/
            // Schedule the alarm!
            mAlarmSender = PendingIntent.getBroadcast(context, i, new Intent(context, AlarmReceiver.class), 0);
            AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, al.get(i).getTime(), mAlarmSender);
            alarmCount++;
        }

    }
    public void cancelAlarms() {
        for (int i = 0; i < alarmCount; i++) {
            mAlarmSender = PendingIntent.getBroadcast(context, i, new Intent(context, AlarmReceiver.class), 0);
            mAlarmSender.cancel();
        }
        alarmCount = 0;
    }
}
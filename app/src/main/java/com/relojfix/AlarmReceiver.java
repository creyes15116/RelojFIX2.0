package com.relojfix;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_DAILY_CHECK = "com.relojfix.DAILY_CHECK";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = (intent == null) ? "" : String.valueOf(intent.getAction());

        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            SettingFix.apply(context, "boot");
            DailyAlarm.scheduleNext(context);
            return;
        }

        if (ACTION_DAILY_CHECK.equals(action)) {
            SettingFix.apply(context, "daily_7_58");
            // Se reprograma para el dia siguiente.
            DailyAlarm.scheduleNext(context);
        }
    }
}

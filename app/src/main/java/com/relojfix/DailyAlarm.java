package com.relojfix;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Calendar;

/**
 * Programa una alarma diaria a las 7:58 AM (hora del reloj) que corrige
 * el ajuste persist.switch_mishow_oob_mode justo antes de las 8:00.
 * La alarma se reprograma sola cada vez que se dispara, y tambien se
 * vuelve a programar en cada arranque (BOOT_COMPLETED), por si se
 * perdio con el apagado.
 */
public final class DailyAlarm {
    private static final String TAG = "RelojFix";
    private static final int HOUR = 7;
    private static final int MINUTE = 58;
    private static final int REQUEST_CODE = 7580;

    private DailyAlarm() {}

    public static void scheduleNext(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am == null) {
            Log.w(TAG, "No hay AlarmManager disponible");
            return;
        }

        Calendar next = Calendar.getInstance();
        next.set(Calendar.HOUR_OF_DAY, HOUR);
        next.set(Calendar.MINUTE, MINUTE);
        next.set(Calendar.SECOND, 0);
        next.set(Calendar.MILLISECOND, 0);
        if (next.getTimeInMillis() <= System.currentTimeMillis()) {
            // Ya paso la hora de hoy; programar para manana.
            next.add(Calendar.DAY_OF_YEAR, 1);
        }

        Intent intent = new Intent(context, AlarmReceiver.class)
                .setAction(AlarmReceiver.ACTION_DAILY_CHECK);
        PendingIntent pi = PendingIntent.getBroadcast(
                context, REQUEST_CODE, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        try {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
            Log.i(TAG, "Proxima revision programada para " + next.getTime());
        } catch (SecurityException se) {
            // Sin el permiso de alarmas exactas; se intenta una version no exacta
            // para que al menos corra, aunque con algo de margen de retraso.
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, next.getTimeInMillis(), pi);
            Log.w(TAG, "Alarma exacta no permitida, programada de forma aproximada", se);
        }
    }
}

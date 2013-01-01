package uz.efir.azon.widget;

import uz.efir.azon.Azon;
import uz.efir.azon.CONSTANT;
import uz.efir.azon.R;
import uz.efir.azon.Schedule;
import uz.efir.azon.VARIABLE;
import uz.efir.azon.util.LocaleManager;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NextNotificationWidgetProvider extends AppWidgetProvider {
    private static final int[] times = new int[]{R.string.fajr, R.string.sunrise, R.string.dhuhr, R.string.asr, R.string.maghrib, R.string.ishaa, R.string.next_fajr};

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        setNextTime(context, appWidgetManager, appWidgetIds);
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    public static void setNextTime(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, NextNotificationWidgetProvider.class));
        setNextTime(context, appWidgetManager, appWidgetIds);
    }
    private static void setNextTime(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        VARIABLE.context = context.getApplicationContext();
        VARIABLE.settings = VARIABLE.context.getSharedPreferences("settingsFile", Context.MODE_PRIVATE);
        new LocaleManager();

        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        if(VARIABLE.settings.getInt("timeFormatIndex", CONSTANT.DEFAULT_TIME_FORMAT) != CONSTANT.DEFAULT_TIME_FORMAT) {
            timeFormat = new SimpleDateFormat("k:mm");
        }
        final int nextTimeIndex = Schedule.today().nextTimeIndex();
        final GregorianCalendar nextTime = Schedule.today().getTimes()[nextTimeIndex];
        for(int i = 0; i < appWidgetIds.length; i++) {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_next_notification);

            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, Azon.class), 0);
            views.setOnClickPendingIntent(R.id.widget_next_notification, pendingIntent);

            views.setTextViewText(R.id.time_name, context.getString(times[nextTimeIndex]));
            views.setTextViewText(R.id.next_notification, timeFormat.format(nextTime.getTime()));

            appWidgetManager.updateAppWidget(appWidgetIds[i], views);
        }
    }
}
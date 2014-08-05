package uz.efir.muazzin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import islam.adhanalarm.widget.NextNotificationWidgetProvider;
import islam.adhanalarm.widget.TimetableWidgetProvider;

/*
 * Following alarm clock intent implementation is adapted from
 *     <a href="https://code.google.com/p/dashclock/">DashClock</a>
 *
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class Utils {
    private static final String[] CLOCK_PACKAGES = new String[] {
        "com.google.android.deskclock", // Google's
        "com.android.deskclock", // AOSP's
        "com.sec.android.app.clockpackage" // Samsung's
    };
    private static final String DEFAULT_ALARM_ACTIVITY = "com.android.deskclock.AlarmClock";
    private static boolean mIsForeground = false;

    public static Intent getDefaultAlarmsIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        for (String packageName : CLOCK_PACKAGES) {
            try {
                ComponentName cn = new ComponentName(packageName, DEFAULT_ALARM_ACTIVITY);
                pm.getActivityInfo(cn, 0);
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(cn);
                return intent;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        // DEFAULT_ALARM_ACTIVITY is not found, launch the main app
        for (String packageName : CLOCK_PACKAGES) {
            try {
                pm.getPackageInfo(packageName, 0);
                return pm.getLaunchIntentForPackage(packageName);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        // TODO: Add an option for user to set a custom app?
        return null;
    }

    public static boolean getIsForeground() {
        return mIsForeground;
    }

    public static void setIsForeground(boolean isForeground) {
        mIsForeground = isForeground;
    }

    public static void updateWidgets(Context context) {
        TimetableWidgetProvider.setLatestTimetable(context);
        NextNotificationWidgetProvider.setNextTime(context);
    }
}

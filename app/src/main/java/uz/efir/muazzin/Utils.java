package uz.efir.muazzin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

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
    private static final String[] DESK_CLOCK_PACKAGES = new String[]{
            "com.google.android.deskclock", // Google's
            "com.android.deskclock", // AOSP's
    };
    private static final String[] OTHER_CLOCK_PACKAGES = new String[]{
            "com.sec.android.app.clockpackage" // Samsung's
    };
    public static boolean isRestartNeeded = false;
    private static boolean mIsForeground = false;

    private static Intent getDefaultClockIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        for (String packageName : DESK_CLOCK_PACKAGES) {
            try {
                pm.getPackageInfo(packageName, 0);
                return pm.getLaunchIntentForPackage(packageName);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        // None worked, try others
        for (String packageName : OTHER_CLOCK_PACKAGES) {
            try {
                pm.getPackageInfo(packageName, 0);
                return pm.getLaunchIntentForPackage(packageName);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        // TODO: Add an option for user to set a custom app?
        return null;
    }

    public static Intent getDefaultAlarmsIntent(Context context) {
        PackageManager pm = context.getPackageManager();
        for (String packageName : DESK_CLOCK_PACKAGES) {
            try {
                ComponentName cn = new ComponentName(packageName, "com.android.deskclock.AlarmClock");
                pm.getActivityInfo(cn, 0);
                return Intent.makeMainActivity(cn);
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }
        return getDefaultClockIntent(context);
    }

    public static boolean getIsForeground() {
        return mIsForeground;
    }

    public static void setIsForeground(boolean isForeground) {
        mIsForeground = isForeground;
    }

//    public static void updateWidgets(Context context) {
//        TimetableWidgetProvider.setLatestTimetable(context);
//        NextNotificationWidgetProvider.setNextTime(context);
//    }
}

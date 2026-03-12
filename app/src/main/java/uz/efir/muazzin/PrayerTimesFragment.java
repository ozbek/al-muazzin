package uz.efir.muazzin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;

public class PrayerTimesFragment extends Fragment {
    private final ArrayList<HashMap<String, String>> mTimeTable = new ArrayList<>(7);
    private PrayerTimesAdapter mTimetableView;
    private TextView mNotes;
    private Preferences mPreferences;

    private final BroadcastReceiver mUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            loadPrayerTimetable();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = Preferences.getInstance(getActivity());
        mTimetableView = new PrayerTimesAdapter(getActivity(), mTimeTable);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_today, container, false);
        mNotes = rootView.findViewById(R.id.notes);
        ListView lv = rootView.findViewById(R.id.timetable);
        lv.setAdapter(mTimetableView);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mPreferences.isLocationSet()) {
            mNotes.setText(null);
        } else {
            mNotes.setText(getString(R.string.location_not_set));
        }
        loadPrayerTimetable();
        ContextCompat.registerReceiver(requireActivity(), mUpdateReceiver,
                new IntentFilter(Utils.ACTION_UPDATE_UI), ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireActivity().unregisterReceiver(mUpdateReceiver);
    }

    private void loadPrayerTimetable() {
        Context context = getActivity();
        if (context == null) {
            return;
        }
        StartNotificationReceiver.setNext(context);
        Schedule today = Schedule.today(context);
//        mTodaysDate.setText(today.hijriDateToString(context));
        GregorianCalendar[] schedule = today.getTimes();
        SimpleDateFormat timeFormat;
        if (DateFormat.is24HourFormat(context)) {
            timeFormat = new SimpleDateFormat("HH:mm ", Locale.getDefault());
        } else {
            timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        }

        mTimeTable.clear();
        for (short i = CONSTANT.FAJR; i <= CONSTANT.NEXT_FAJR; i++) {
            HashMap<String, String> map = new HashMap<>();
            map.put("time_name", getString(CONSTANT.TIME_NAMES[i]));
            map.put("time", timeFormat.format(schedule[i].getTime()));
            mTimeTable.add(map);
        }

        mTimetableView.setNextPrayerIndex(today.nextTimeIndex());
        mTimetableView.notifyDataSetChanged();
    }

    private class PrayerTimesAdapter extends BaseAdapter {
        private final Context mContext;
        private final ArrayList<HashMap<String, String>> mPrayerTimes;
        private int mNextPrayerIndex = -1;

        public PrayerTimesAdapter(Context context, ArrayList<HashMap<String, String>> data) {
            mContext = context;
            mPrayerTimes = data;
        }

        public void setNextPrayerIndex(int index) {
            mNextPrayerIndex = index;
        }

        @Override
        public int getCount() {
            return mPrayerTimes.size();
        }

        @Override
        public HashMap<String, String> getItem(int position) {
            return mPrayerTimes.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.timetable_row, parent, false);
            }

            HashMap<String, String> prayerTime = getItem(position);
            String timeName = prayerTime.get("time_name");
            if (position == mNextPrayerIndex) {
                timeName = getString(R.string.next_time_marker) + timeName;
            }
            TextView timeNameView = convertView.findViewById(R.id.time_name);
            timeNameView.setText(timeName);

            TextView timeView = convertView.findViewById(R.id.time);
            timeView.setText(prayerTime.get("time"));

            // Zebra stripes
            if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.darker_gray);
                timeNameView.setTextColor(0xff000000);
                timeView.setTextColor(0xff000000);
            } else {
                convertView.setBackgroundResource(android.R.color.transparent);
                // Assuming default text colors are correct for transparent background
            }

            return convertView;
        }
    }
}

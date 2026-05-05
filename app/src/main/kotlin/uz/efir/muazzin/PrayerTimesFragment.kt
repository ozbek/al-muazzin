package uz.efir.muazzin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import java.text.SimpleDateFormat
import java.util.Locale

class PrayerTimesFragment : Fragment() {
    private val mTimeTable = ArrayList<HashMap<String?, String?>>(7)
    private var mTimetableView: PrayerTimesAdapter? = null
    private var mTodaysDate: TextView? = null
    private var mNotes: TextView? = null
    private lateinit var mPreferences: Preferences

    private val mUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            loadPrayerTimetable()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPreferences = Preferences.getInstance(requireActivity().baseContext)
        mTimetableView = PrayerTimesAdapter(requireActivity().baseContext, mTimeTable)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val rootView = inflater.inflate(R.layout.tab_today, container, false)
        mTodaysDate = rootView.findViewById(R.id.today)
        mNotes = rootView.findViewById(R.id.notes)
        val lv = rootView.findViewById<ListView>(R.id.timetable)
        lv.adapter = mTimetableView
        lv.isNestedScrollingEnabled = true

        return rootView
    }

    override fun onResume() {
        super.onResume()
        if (mPreferences.isLocationSet) {
            mNotes?.text = null
        } else {
            mNotes?.text = getString(R.string.location_not_set)
        }
        loadPrayerTimetable()
        ContextCompat.registerReceiver(
            requireActivity(),
            mUpdateReceiver,
            IntentFilter(ACTION_UPDATE_UI),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(mUpdateReceiver)
    }

    private fun loadPrayerTimetable() {
        val context: Context = requireActivity().baseContext ?: return
        PrayerAlarmScheduler.setNext(context)
        val today = PrayerSchedule(mPreferences)
        mTodaysDate?.text = today.hijriDateToString(context)
        val schedule = today.times
        val timeFormat = if (DateFormat.is24HourFormat(context)) {
            SimpleDateFormat("HH:mm ", Locale.getDefault())
        } else {
            SimpleDateFormat("hh:mm a", Locale.getDefault())
        }

        mTimeTable.clear()
        for (i in PrayerTime.FAJR.ordinal..PrayerTime.NEXT_FAJR.ordinal) {
            val map = HashMap<String?, String?>()
            map["time_name"] = getString(PrayerTime.entries[i].labelRes)
            map["time"] = timeFormat.format(schedule[i].getTime())
            mTimeTable.add(map)
        }

        mTimetableView?.setNextPrayerIndex(today.nextTimeIndex())
        mTimetableView?.notifyDataSetChanged()
    }

    companion object {
        const val ACTION_UPDATE_UI: String = "uz.efir.muazzin.action.UPDATE_UI"
    }

    private inner class PrayerTimesAdapter(
        private val mContext: Context?,
        private val mPrayerTimes: ArrayList<HashMap<String?, String?>>
    ) : BaseAdapter() {
        private var mNextPrayerIndex = -1

        fun setNextPrayerIndex(index: Int) {
            mNextPrayerIndex = index
        }

        override fun getCount(): Int {
            return mPrayerTimes.size
        }

        override fun getItem(position: Int): HashMap<String?, String?> {
            return mPrayerTimes[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var convertView = convertView
            if (convertView == null) {
                convertView =
                    LayoutInflater.from(mContext).inflate(R.layout.timetable_row, parent, false)
            }

            val prayerTime = getItem(position)
            var timeName = prayerTime["time_name"]
            if (position == mNextPrayerIndex) {
                timeName = getString(R.string.next_time_marker) + timeName
            }
            val timeNameView = convertView.findViewById<TextView>(R.id.time_name)
            timeNameView.text = timeName

            val timeView = convertView.findViewById<TextView>(R.id.time)
            timeView.text = prayerTime["time"]

            val context = convertView.context
            val textColor = if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.dark_gray)
                ContextCompat.getColor(context, android.R.color.black)
            } else {
                convertView.setBackgroundResource(android.R.color.transparent)
                ContextCompat.getColor(context, R.color.light_gray)
            }
            timeNameView.setTextColor(textColor)
            timeView.setTextColor(textColor)

            return convertView
        }
    }
}

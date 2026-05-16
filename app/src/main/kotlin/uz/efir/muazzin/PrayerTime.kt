package uz.efir.muazzin

import androidx.annotation.StringRes

enum class PrayerTime(@param:StringRes val labelRes: Int) {
    FAJR(R.string.fajr),
    SUNRISE(R.string.sunrise),
    DHUHR(R.string.dhuhr),
    ASR(R.string.asr),
    MAGHRIB(R.string.maghrib),
    ISHAA(R.string.ishaa),
    NEXT_FAJR(R.string.next_fajr),
}

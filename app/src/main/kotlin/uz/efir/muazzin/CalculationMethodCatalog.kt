package uz.efir.muazzin

import com.batoulapps.adhan2.CalculationMethod

/**
 * Maps the persisted calculationMethodIndex preference to an adhan2 CalculationMethod, and
 * picks a sensible default method based on the user's locale country code.
 *
 * Index to method mapping is part of the persistence contract: if you change the set or
 * the order, bump Preferences.PREFS_VERSION and add a migration.
 */
object CalculationMethodCatalog {
    private data class Entry(
        val method: CalculationMethod,
        val countries: Set<String>,
    )

    private val entries: List<Entry> = listOf(
        // 0: EGYPTIAN — Africa, Syria, Iraq, Lebanon, Malaysia
        Entry(
            CalculationMethod.EGYPTIAN, setOf(
                "AGO", "BDI", "BEN", "BFA", "BWA", "CAF", "CIV", "CMR", "COG", "COM",
                "CPV", "DJI", "DZA", "EGY", "ERI", "ESH", "ETH", "GAB", "GHA", "GIN",
                "GMB", "GNB", "GNQ", "KEN", "LBR", "LBY", "LSO", "MAR", "MDG", "MLI",
                "MOZ", "MRT", "MUS", "MWI", "MYT", "NAM", "NER", "NGA", "REU", "RWA",
                "SDN", "SEN", "SLE", "SOM", "STP", "SWZ", "SYC", "TCD", "TGO", "TUN",
                "TZA", "UGA", "ZAF", "ZAR", "ZWB", "ZWE",
                "IRQ", "LBN", "MYS", "SYR",
            )
        ),
        // 1: KARACHI — Afghanistan, Bangladesh, India, Pakistan, Uzbekistan
        Entry(CalculationMethod.KARACHI, setOf("AFG", "BGD", "IND", "PAK", "UZB")),
        // 2: NORTH_AMERICA — USA, Canada
        Entry(CalculationMethod.NORTH_AMERICA, setOf("USA", "CAN")),
        // 3: MUSLIM_WORLD_LEAGUE — Europe + Far East
        Entry(
            CalculationMethod.MUSLIM_WORLD_LEAGUE, setOf(
                "AND", "AUT", "BEL", "DNK", "FIN", "FRA", "DEU", "GIB", "IRL", "ITA",
                "LIE", "LUX", "MCO", "NLD", "NOR", "PRT", "SMR", "ESP", "SWE", "CHE",
                "GBR", "VAT",
                "CHN", "JPN", "PRK", "TWN",
            )
        ),
        // 4: UMM_AL_QURA — Arabian Peninsula + South Korea
        Entry(
            CalculationMethod.UMM_AL_QURA,
            setOf("BHR", "KWT", "OMN", "QAT", "SAU", "YEM", "KOR")
        ),
    )

    const val DEFAULT_INDEX: Int = 4 // UMM_AL_QURA

    fun method(index: Int): CalculationMethod = entries[index].method

    fun indexForCountry(iso3CountryCode: String): Int? =
        entries.indices.firstOrNull { iso3CountryCode in entries[it].countries }
}

package dk.hjlab.minvaegt

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

object AppStorage {
    private const val PREFS = "min_vaegt"
    private const val PROFILE = "profile"
    private const val WEIGHTS = "weights"

    fun loadProfile(context: Context): Profile {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(PROFILE, null) ?: return Profile()
        return runCatching {
            val json = JSONObject(raw)
            Profile(
                name = json.optString("name"),
                heightCm = json.optDouble("heightCm", 170.0),
                startWeight = json.optDouble("startWeight", 80.0),
                goalWeight = json.optDouble("goalWeight", 75.0),
                startDate = json.optString("startDate", LocalDate.now().toString())
            )
        }.getOrDefault(Profile())
    }

    fun saveProfile(context: Context, profile: Profile) {
        val json = JSONObject()
            .put("name", profile.name)
            .put("heightCm", profile.heightCm)
            .put("startWeight", profile.startWeight)
            .put("goalWeight", profile.goalWeight)
            .put("startDate", profile.startDate)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(PROFILE, json.toString()).apply()
    }

    fun loadWeights(context: Context): List<WeightEntry> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(WEIGHTS, "[]") ?: "[]"
        return runCatching {
            val json = JSONArray(raw)
            buildList {
                for (i in 0 until json.length()) {
                    val item = json.getJSONObject(i)
                    add(WeightEntry(item.getDouble("weight"), item.getLong("timestamp")))
                }
            }.sortedBy { it.timestamp }
        }.getOrDefault(emptyList())
    }

    fun saveWeights(context: Context, entries: List<WeightEntry>) {
        val json = JSONArray()
        entries.sortedBy { it.timestamp }.forEach {
            json.put(JSONObject().put("weight", it.weight).put("timestamp", it.timestamp))
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(WEIGHTS, json.toString()).apply()
    }
}

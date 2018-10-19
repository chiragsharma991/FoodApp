package dk.eatmore.foodapp.storage

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager

object PreferenceUtil {

    private var sharedPreferences: SharedPreferences? = null

    private var editor: SharedPreferences.Editor? = null

    /**
     * returns map of all the key value pair available in SharedPreference
     *
     * @return Map<String></String>, ?>
     */
    val all: Map<String, *>
        get() = sharedPreferences!!.all

    /**
     * Initialize the SharedPreferences instance for the app.
     * This method must be called before using any other methods of this class.
     */

    fun init(mcontext: Context) {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mcontext)
            editor = sharedPreferences!!.edit()
        }
    }

    /**
     * Puts new Key and its Values into SharedPreference map.
     *
     * @param key
     * @param value
     */
    fun putValue(key: String, value: String) {
        editor!!.putString(key, value)
    }

    /**
     * Puts new Key and its Values into SharedPreference map.
     *
     * @param key
     * @param value
     */
    fun putValue(key: String, value: Int) {
        editor!!.putInt(key, value)
    }

    /**
     * Puts new Key and its Values into SharedPreference map.
     *
     * @param key
     * @param value
     */
    fun putValue(key: String, value: Long) {
        editor!!.putLong(key, value)
    }

    /**
     * Puts new Key and its Values into SharedPreference map.
     *
     * @param key
     * @param value
     */
    fun putValue(key: String, value: Boolean) {
        editor!!.putBoolean(key, value)
    }

    /**
     * saves the values from the editor to the SharedPreference
     */
    fun clearAll() {
        editor!!.clear()
    }

    fun remove(key : String) {
        editor!!.remove(key)
    }


    fun save() {
        editor!!.commit()
    }

    /**
     * returns a values associated with a Key default value ""
     *
     * @return String
     */
    fun getString(key: String, defValue: String): String? {
        return sharedPreferences!!.getString(key, defValue)
    }

    /**
     * returns a values associated with a Key default value -1
     *
     * @return String
     */
    fun getInt(key: String, defValue: Int): Int {
        return sharedPreferences!!.getInt(key, defValue)
    }

    /**
     * returns a values associated with a Key default value -1
     *
     * @return String
     */
    fun getLong(key: String, defValue: Long): Long {
        return sharedPreferences!!.getLong(key, defValue)
    }

    /**
     * returns a values associated with a Key default value false
     *
     * @return String
     */
    fun getBoolean(key: String, defValue: Boolean): Boolean {
        return sharedPreferences!!.getBoolean(key, defValue)
    }

    /**
     * Checks if key is exist in SharedPreference
     *
     * @param key
     * @return boolean
     */
    operator fun contains(key: String): Boolean {
        return sharedPreferences!!.contains(key)
    }



    val USER_NAME = "user_name"
    val USER_ID = "id"
    val CUSTOMER_ID = "customer_id"
    val R_TOKEN = "r_token"
    val E_MAIL = "restaurant_name"
    val R_KEY = "r_key"
    val DEVICE_TOKEN = "device_token"
    val LANGUAGE = "language"
    val FIRST_NAME = "first_name"
    val PHONE = "keep_screen_on"
    val LOGIN_FROM = "login_from"
    val KSTATUS = "kStatus"


}
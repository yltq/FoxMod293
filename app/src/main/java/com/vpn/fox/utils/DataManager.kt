package com.vpn.fox.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.vpn.fox.VpnApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

object DataManager {
    val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    var completeGuide by BoolData()
    var requestNf by BoolData()
    var connectIp by StringData()

    var daon by StringData() //总控参数
    var peihr by StringData() //smart服务器列表
    var aiec by StringData() //服务器列表
    var fm_ads by StringData() //广告配置
    var togging by StringData() //cloak缓存
    var consentDebugSettingEnable by BoolData() //是否已经展示过欧盟广告弹窗

    private class StringData : ReadWriteProperty<DataManager, String> {
        override fun getValue(thisRef: DataManager, property: KProperty<*>): String {
            val key = stringPreferencesKey("data_${property.name}_store")
            return runBlocking {
                VpnApp.app.dataStore.data.first()[key] ?: ""
            }
        }

        override fun setValue(thisRef: DataManager, property: KProperty<*>, value: String) {
            val key = stringPreferencesKey("data_${property.name}_store")
            runBlocking {
                VpnApp.app.dataStore.edit { data ->
                    data[key] = value
                }
            }
        }
    }

    private class LongData : ReadWriteProperty<DataManager, Long> {
        override fun getValue(thisRef: DataManager, property: KProperty<*>): Long {
            val key = longPreferencesKey("data_${property.name}_store")
            return runBlocking {
                VpnApp.app.dataStore.data.first()[key] ?: 0L
            }
        }

        override fun setValue(thisRef: DataManager, property: KProperty<*>, value: Long) {
            val key = longPreferencesKey("data_${property.name}_store")
            runBlocking {
                VpnApp.app.dataStore.edit { data ->
                    data[key] = value
                }
            }
        }
    }


    private class BoolData : ReadWriteProperty<DataManager, Boolean> {
        override fun getValue(thisRef: DataManager, property: KProperty<*>): Boolean {
            val key = booleanPreferencesKey("data_${property.name}_store")
            return runBlocking {
                VpnApp.app.dataStore.data.first()[key] ?: false
            }
        }

        override fun setValue(thisRef: DataManager, property: KProperty<*>, value: Boolean) {
            val key = booleanPreferencesKey("data_${property.name}_store")
            runBlocking {
                VpnApp.app.dataStore.edit { data ->
                    data[key] = value
                }
            }
        }
    }
}
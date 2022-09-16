package com.lagradost.cloudstream3.utils.resources

import android.content.res.Resources
import android.util.Log
import com.lagradost.cloudstream3.AcraApplication
import com.lagradost.cloudstream3.AcraApplication.Companion.getActivity
import com.lagradost.cloudstream3.AcraApplication.Companion.getKey
import com.lagradost.cloudstream3.AcraApplication.Companion.setKey
import com.lagradost.cloudstream3.MainActivity.Companion.afterPluginsLoadedEvent

typealias PatchFactory = (Resources) -> ResourcePatch

object ResourcePackManager {
    private const val KEY = "ResourcePackManager"
    const val SETTINGS_KEY = "active_resource_pack_key" // TODO
    val packs: HashMap<String, PatchFactory> = hashMapOf()
    private val pluginMapping: HashMap<String, ArrayList<String>> = hashMapOf()
    var activePackId: String? = null
        internal set
    val activePack: PatchFactory?
        get() = if (activePackId == null) null else packs[activePackId]

    init {
        afterPluginsLoadedEvent += ::onExtensionsLoaded
    }

    fun registerPack(name: String, factory: PatchFactory, pluginId: String?) {
        packs[name] = factory
        if (pluginId != null) {
            if (!pluginMapping.containsKey(pluginId))
                pluginMapping[pluginId] = arrayListOf()
            pluginMapping[pluginId]?.add(name)
        }
    }

    fun unregisterPlugin(pluginId: String?) {
        pluginMapping[pluginId]?.forEach {
            packs.remove(it)
            if (activePackId == it) // if pack is being removed make sure its not set
                selectPack(null)
        }
        pluginMapping.remove(pluginId)
    }

    fun selectPack(packId: String?) {
        activePackId = if (packId == null) null
        else if (packs.containsKey(packId)) packId
        else null

        Log.d(KEY, "Selecting: ${activePackId ?: "none"}")
        setKey(SETTINGS_KEY, activePackId)
    }


    private fun onExtensionsLoaded(successful: Boolean = false) {
        if (!successful) return
        activePackId = getKey(SETTINGS_KEY)
        Log.d(KEY, "Selecting saved: ${activePackId ?: "none"}")
    }

}
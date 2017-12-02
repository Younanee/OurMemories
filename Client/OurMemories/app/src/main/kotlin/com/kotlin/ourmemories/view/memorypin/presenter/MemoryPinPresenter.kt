package com.kotlin.ourmemories.view.memorypin.presenter

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Handler
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.util.Log
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.kotlin.ourmemories.view.memoryview.MemoryViewActivity
import com.kotlin.ourmemories.R
import com.kotlin.ourmemories.data.source.memory.Memory
import com.kotlin.ourmemories.data.source.memory.MemoryRepository
import com.kotlin.ourmemories.view.memorypin.MemoryPinFragment
import org.jetbrains.anko.*

/**
 * Created by kimmingyu on 2017. 11. 12..
 */
class MemoryPinPresenter:MemoryPinContract.Presenter {
    lateinit override var mView: MemoryPinContract.View
    lateinit override var fragment: MemoryPinFragment
    override var mGoogleApiClient: GoogleApiClient? = null
    lateinit override var memoryData: MemoryRepository

    override fun isMemoryCheck(classification:Int) {
        val locationManger = fragment.activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val permission = arrayOf(android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION)
        // 권한체크
        if ((ActivityCompat.checkSelfPermission(fragment.context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                or (ActivityCompat.checkSelfPermission(fragment.context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(fragment.activity, permission, 0)
        } else {
            // GPS 가 꺼져있는지 검사후 꺼있으면 켜는 화면으로 전환
            if (!locationManger.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                fragment.startActivity(intent)
            } else {
                fragment.showDialog()
                // GPS 좀더 찾기
                val handler = Handler()
                handler.postDelayed({
                    val location: Location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
                    // 내장 디비에 있는지 검사
                    memoryData.getLocalMemory(classification, location.latitude, location.longitude)
                },4000)

            }
        }
    }
    fun userChooseDialog(items:List<Memory>?){
        fragment.hideDialog()
        if(items != null){
            val memories = mutableListOf<String>()
            for(i in 0 until items!!.size){
                memories.add(i, items[i].title)
            }
            fragment.activity.selector("Memories", memories, { dialogInterface, i ->
                fragment.activity.startActivity<MemoryViewActivity>("id" to items[i].id)
            })
        }else {
            fragment.activity.alert(fragment.activity.resources.getString(R.string.error_message_memory), "Memories") {
                yesButton { }
            }.show()
        }
    }
}
package com.kotlin.ourmemories.view.memorylist

import android.content.Intent
import android.database.Cursor
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.kotlin.ourmemories.DB.DBManagerMemory

/**
 * Created by nyoun_000 on 2017-11-11.
 */
class MemoryListMapFragment : SupportMapFragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var nationName: String

    override fun onMapReady(map: GoogleMap?) {
        mMap = map as GoogleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isMapToolbarEnabled = true


        //초기 map셋팅을 나라에 카메라 이동되는 로직
        val intent: Intent? = activity.intent
        if (intent != null) {
            nationName = activity.intent.extras.getString("nationName")
            //context.toast(nationName)
            if (nationName == "대한민국") {
                val kor = LatLng(37.574515, 126.976930)
                moveToMapCameraPosition(kor,10.0f)
            } else if (nationName == "일본") {
                val jpn = LatLng(35.709261, 139.731070)
                moveToMapCameraPosition(jpn,10.0f)
            } else if (nationName == "미국") {
                val usa = LatLng(38.906414, -77.040912)
                moveToMapCameraPosition(usa,10.0f)
            }

            //지도에 마커 생성을 위한 데이터베이스 쿼리
            var cursor: Cursor = DBManagerMemory.getMemoriesWithCursor(nationName)

            //지도에 마커 생성
            if (cursor.moveToFirst()) {
                for (n in 0..cursor.columnCount - 1) {
                    val spot = LatLng(cursor.getString(2).toDouble(), cursor.getString(3).toDouble())
                    mMap.addMarker(MarkerOptions().position(spot).title(cursor.getString(1)))
                    cursor.moveToNext()
                }
            }
        }
    }

    //해당 latLng로 카메라 확대 및 이동
    fun moveToMapCameraPosition(latLng: LatLng, f : Float){
        var cameraPostion : CameraPosition.Builder = CameraPosition.Builder()
        cameraPostion.target(latLng)
        cameraPostion.zoom(f)
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPostion.build()))
    }


}
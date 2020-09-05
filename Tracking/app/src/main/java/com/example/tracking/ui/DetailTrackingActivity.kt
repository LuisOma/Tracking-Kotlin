package com.example.tracking.ui

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tracking.R
import com.example.tracking.utils.TrackingUtility
import kotlinx.android.synthetic.main.activity_detail_tracking.*


class DetailTrackingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_tracking)
        val byteArray = intent.getByteArrayExtra("image")
        val bmp = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)
        val name = getIntent().getStringExtra("name")
        val time = getIntent().getLongExtra("time", 0)
        val distance = getIntent().getIntExtra("distance", 0)
        val km = distance / 1000f
        val totalDistance = (km * 10) / 10f

        ivTrackImage.setImageBitmap(bmp)
        tvName.text = name.toString()
        tvDate.text = TrackingUtility.getFormattedStopWatchTime(time)
        tvDistance.text = "$totalDistance kms"

        btnShare.setOnClickListener {
            val message: String =
                "Recorriste " + tvDistance.text.toString() + " de la ruta " + tvName.text.toString() + " en un tiempo de " + tvDate.text.toString()
            val intent = Intent()
            intent.action = Intent.ACTION_SEND
            intent.putExtra(Intent.EXTRA_TEXT, message)
            intent.type = "text/plain"
            startActivity(Intent.createChooser(intent, "Favor de seleccionar la app: "))
        }
    }
}

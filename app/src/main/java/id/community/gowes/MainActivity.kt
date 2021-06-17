package id.community.gowes

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import id.community.gowes.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by contentView(R.layout.activity_main)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.navHostFragment
    }
}

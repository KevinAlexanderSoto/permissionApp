package com.kalex.permissionapp

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.kalex.permissionapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { _ ->
            if (allPermissionsGranted()) {
                startFragment()
            } else {
                aksToPermission(REQUIRED_PERMISSIONS)
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }
    private fun aksToPermission(REQUIRED_PERMISSIONS: Array<String>) {
        requestPermissionLauncher.launch(REQUIRED_PERMISSIONS)
    }
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { isGranted: Map<String, Boolean> ->
            val permissionToAsk = mutableListOf<String>()

            if (isGranted.values.contains(false)) {
                REQUIRED_PERMISSIONS.forEach {
                    when {
                        shouldShowRequestPermissionRationale(it) -> {
                            //Usuario rechazo los permisos, pero se pueden pedir otra vez
                            permissionToAsk.add(it)
                        }
                    }
                }
                if (permissionToAsk.size > 0) {
                    executeDialogForNegativePermission(true) {
                        aksToPermission(permissionToAsk.toTypedArray())
                    }
                } else {
                    //Usuario rechazo los permisos y marco la opcion de no mostrar otra vez
                    executeDialogForNegativePermission(false) {

                    }
                }

            } else {
                //acepto los permisos
                startFragment()
            }
        }
    private fun startFragment() {
        SecondFragment().show(supportFragmentManager, FRAGMENT_TAG)
    }
    private fun executeDialogForNegativePermission(isRationale :Boolean,callback : ()->Unit){
        //TODO: Add strings resources and style
        MaterialAlertDialogBuilder(this)
            .setTitle("ACEPTE LOS PERMISOS")
            .setMessage("Si no los aceptas, no vas a poder tener mas perritos en la coleccion." +
                    "Intenta ejecutar la camara otra vez, y dale al boton de ACEPTAR!!!!"
            )
            .setPositiveButton("Vale, voy a reflexionar ") { dialog, _ ->
                callback.invoke()
                if(!isRationale){
                    //Take the User to the app settings
                    val intent = Intent(
                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", packageName, null)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }
                dialog.dismiss()
            }
            .show()
}


    companion object {
        private const val FRAGMENT_TAG = "second_fragment"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,

                ).toTypedArray()
    }
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
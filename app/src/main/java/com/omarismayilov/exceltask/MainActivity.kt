package com.omarismayilov.exceltask

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.omarismayilov.exceltask.databinding.ActivityMainBinding


data class Product(val id: String, val name: String, val category: String, val price: Double)

class MainActivity : AppCompatActivity() {
    private val PERMISSION_CODE = 101
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkPermissions()) {
            Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show()
        } else {
            requestPermission()
        }
        val data = List(200) {
            Product(
                id = "${it + 1}",
                name = "Product ${it + 1}",
                category = "Category",
                price = (it + 1) * 10.0
            )
        }

        val infoMap = mapOf(
            "Cari (kod/ad)" to "12546/EXAMPLE COMPANY NAME",
            "Qaimə №" to "565498",
            "Filial" to "00086000000473456",
            "İstifadəçi" to "Example user",
            "Tarix" to "20.02.2023",
        )


        if (checkPermissions()) {
            binding.btnGenerate.setOnClickListener {
                val generator = PdfTableConverter<Product>(
                    this,
                    "task_1234567",
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                )

                generator.apply {
                    /*   addPrimaryLogo()
                    addAdditionalLogo()
                    addCompanyName("")
                    addBarcode("12345XXX5841X64158641XXXX6541")
                    addInfoTable(infoMap)
                    addDataTable(
                        data = data,
                        columnNames = arrayOf(
                            "Product id",
                            "Product Name",
                            "Product Category",
                            "Product price"
                        ),
                        cellProviders = listOf(
                            { product: Product -> product.id },
                            { product: Product -> product.name },
                            { product: Product -> product.category },
                            { product: Product -> product.price.toString() }
                        )
                    )
                    closePdf()
                }

                Toast.makeText(this@MainActivity,"Generated",Toast.LENGTH_SHORT).show()*/
                }


            }
        }
    }

    private fun checkPermissions(): Boolean {
        val writeStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            WRITE_EXTERNAL_STORAGE
        )
        val readStoragePermission = ContextCompat.checkSelfPermission(
            applicationContext,
            READ_EXTERNAL_STORAGE
        )


        return writeStoragePermission == PackageManager.PERMISSION_GRANTED &&
                readStoragePermission == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
            ),
            PERMISSION_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == PERMISSION_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Permissions Granted..", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permissions Denied..", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}





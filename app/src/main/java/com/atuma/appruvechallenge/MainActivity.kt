package com.atuma.appruvechallenge

import android.Manifest
import android.Manifest.permission.*
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.*
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.*
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    private val TAG = javaClass.simpleName

    private val PICK_CAMERA_IMAGE = 2
    private val PICK_GALLERY_IMAGE = 1

    val DATE_FORMAT = "yyyyMMdd_HHmmss"
    val IMAGE_DIRECTORY = "ImageScalling"
    private val PERMISSION_REQUEST_CODE = 200

    private val SCHEME_FILE = "file"
    private val SCHEME_CONTENT = "content"

    private var btnGallery: Button? = null
    private var btnCamera: Button? = null
    private var btnCompress: Button? = null

    private var img: ImageView? = null
    private var imgCompress: ImageView? = null

    private var imageCaptureUri: Uri? = null

    private var file: File? = null
    private var sourceFile: File? = null
    private var destFile: File? = null

    private var dateFormatter: SimpleDateFormat? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val m: Method = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
        setContentView(R.layout.activity_main)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE,
                    READ_EXTERNAL_STORAGE, INTERNET), PERMISSION_REQUEST_CODE)
        }
        file = File(Environment.getExternalStorageDirectory()
                .toString() + "/" + IMAGE_DIRECTORY)
        if (!file!!.exists()) {
            file!!.mkdirs()
        }

        dateFormatter = SimpleDateFormat(
                DATE_FORMAT, Locale.US)

        initView()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (permissions.size > 0 && grantResults.size > 0) {
                var flag = true
                for (i in grantResults.indices) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        flag = false
                    }
                }
//                if (flag) {
//                    openActivity()
//                } else {
//                    finish()
//                }
//            } else {
//                finish()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    fun initView() {
        btnGallery = findViewById(R.id.activity_main_btn_load_from_gallery) as Button
        btnCamera = findViewById(R.id.activity_main_btn_load_from_camera) as Button
        btnCompress = findViewById(R.id.activity_main_btn_compress) as Button
        img = findViewById(R.id.activity_main_img) as ImageView
        imgCompress = findViewById(R.id.activity_main_img_compress) as ImageView
        btnGallery!!.setOnClickListener(this)
        btnCamera!!.setOnClickListener(this)
        btnCompress!!.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.activity_main_btn_load_from_gallery -> {
                val intentGalley = Intent(Intent.ACTION_PICK)
                intentGalley.type = "image/*"
                startActivityForResult(intentGalley, PICK_GALLERY_IMAGE)
            }
            R.id.activity_main_btn_load_from_camera -> {
                destFile = File(file, "img_"
                        + dateFormatter!!.format(Date()).toString() + ".png")
                imageCaptureUri = Uri.fromFile(destFile)
                val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, imageCaptureUri)
                startActivityForResult(intentCamera, PICK_CAMERA_IMAGE)
            }
            R.id.activity_main_btn_compress -> {
                val bmp: Bitmap? = compressImage(destFile!!)
                imgCompress!!.setImageBitmap(bmp)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {

                PICK_CAMERA_IMAGE -> {
                    Log.d("$TAG.PICK_CAMERA_IMAGE", "Selected image uri path :$imageCaptureUri")
                    img!!.setImageURI(imageCaptureUri)
                }
            }
        }
    }


    fun compressImage(f: File): Bitmap? {
//        val filePath = getRealPathFromURI(imageUri)
        var b: Bitmap? = null
        val o = BitmapFactory.Options()

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        o.inJustDecodeBounds = true
        var fis: FileInputStream? = null
        try {
            fis = FileInputStream(f)
            BitmapFactory.decodeStream(fis, null, o)
            fis.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
//        var bmp = BitmapFactory.decodeFile(file, o)
        var actualHeight = o.outHeight
        var actualWidth = o.outWidth

//      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = actualWidth / actualHeight.toFloat()
        val maxRatio = maxWidth / maxHeight

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

        val o2 = BitmapFactory.Options()

//      setting inSampleSize value allows to load a scaled down version of the original image
        o2.inSampleSize = calculateInSampleSize(o, actualWidth, actualHeight)

//      inJustDecodeBounds set to false to load the actual bitmap
        o2.inJustDecodeBounds = false

//      this options allow android to claim the bitmap memory if it runs low on memory
        o2.inPurgeable = true
        o2.inInputShareable = true
        o2.inTempStorage = ByteArray(16 * 1024)
        try {
//          load the bitmap from its path
            fis = FileInputStream(f)
            b = BitmapFactory.decodeStream(fis, null, o2)
            fis.close()
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        destFile = File(file, "img_"
                + dateFormatter!!.format(Date()).toString() + ".png")
        try {
            val out = FileOutputStream(destFile)
            b?.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
        } catch (exception: Exception) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / o.outWidth.toFloat()
        val ratioY = actualHeight / o.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)

        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(destFile)

//          write the compressed bitmap at the destination specified by filename.
            b?.compress(Bitmap.CompressFormat.JPEG, 80, out)
            out.flush()
            out.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return b
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = width * height.toFloat()
        val totalReqPixelsCap = reqWidth * reqHeight * 2.toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }
}


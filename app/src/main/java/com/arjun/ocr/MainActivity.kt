package com.arjun.ocr

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // Choose either Latin or Devanagari recognizer as per your requirement
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    // private val recognizer = TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())

    private val PICK_IMAGE_REQUEST = 1
    private val READ_PERMISSION_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check and request permission if needed
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), READ_PERMISSION_CODE)
        } else {
            openGallery()
        }
    }

    // Function to open the gallery
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == READ_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            openGallery()
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            val uri: Uri? = data.data
            if (uri != null) {
                try {
                    val image = InputImage.fromFilePath(this, uri)

                    // Process the image with the recognizer
                    recognizer.process(image)
                        .addOnSuccessListener { visionText ->
                            val resultText = visionText.text
                            for (block in visionText.textBlocks) {
                                val blockText = block.text
                                val blockCornerPoints = block.cornerPoints
                                val blockFrame = block.boundingBox
                                for (line in block.lines) {
                                    val lineText = line.text
                                    val lineCornerPoints = line.cornerPoints
                                    val lineFrame = line.boundingBox
                                    Log.d("frame", "$lineText : ${lineFrame?.centerX()} ${lineFrame?.centerY()}")
                                    for (element in line.elements) {
                                        val elementText = element.text
                                        val elementCornerPoints = element.cornerPoints
                                        val elementFrame = element.boundingBox
                                        Log.d("elementFrame", "$elementText : $elementFrame")
                                        // You can now use the elementText and elementFrame (bounding box) for your needs
                                    }
                                }
                            }
                            Toast.makeText(this, "OCR Successful: $resultText", Toast.LENGTH_LONG).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "OCR Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }

                    // Display the selected image in the ImageView
                    val imageView: ImageView = findViewById(R.id.imageView)
                    imageView.setImageURI(uri)

                } catch (e: IOException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
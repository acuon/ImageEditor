package dev.acuon.imageeditor.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import dev.acuon.imageeditor.R
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*


internal object BitmapUtils {

    /**
     * Deletes image file for a given path.
     *
     * @param context   The application context.
     * @param imagePath The path of the photo to be deleted.
     */
    fun deleteImageFile(context: Context, imagePath: String?): Boolean {

        // Get the file
        val imageFile = File(imagePath)

        // Delete the image
        val deleted = imageFile.delete()

        // If there is an error deleting the file, show a Toast
        if (!deleted) {
            val errorMessage = context.getString(R.string.error)
        }
        return deleted
    }

    /**
     * Helper method for adding the photo to the system photo gallery so it can be accessed
     * from other apps.
     *
     * @param imagePath The path of the saved image
     */
    private fun galleryAddPic(context: Context, imagePath: String?) {
        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val f = File(imagePath)
        val contentUri = Uri.fromFile(f)
        mediaScanIntent.data = contentUri
        context.sendBroadcast(mediaScanIntent)
    }

    /**
     * Helper method for saving the image.
     *
     * @param context The application context.
     * @param image   The image to be saved.
     * @return The path of the saved image.
     */
    fun saveImage(context: Context, image: Bitmap): String? {
        var savedImagePath: String? = null

        // Create the new file in the external storage
        val timeStamp = SimpleDateFormat(
            "yyyyMMdd_HHmmss",
            Locale.getDefault()
        ).format(Date())
        val imageFileName = "JPEG_$timeStamp.jpg"
        val storageDir = File(
            FILE_PATH
        )
        var success = true
        if (!storageDir.exists()) {
            success = storageDir.mkdirs()
        }

        // Save the new Bitmap
        if (success) {
            val imageFile = File(storageDir, imageFileName)
            savedImagePath = imageFile.absolutePath
            try {
                val fOut: OutputStream = FileOutputStream(imageFile)
                image.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Add the image to the system gallery
//            galleryAddPic(context, savedImagePath)

            // Show a Toast with the save location
            // String savedMessage = context.getString(R.string.saved_message, savedImagePath);
        }
        return savedImagePath
    }

    /**
     * Helper method for saving the image.
     *
     * @param context The application context.
     * @param image   The image to be saved.
     * @return The path of the saved image.
     */
    fun saveImageUsingUri(context: Context, uri: Uri): String? {
        val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        return saveImage(context, bitmap)
    }

    /**
     * Helper method for sharing an image.
     *
     * @param context   The image context.
     * @param imagePath The path of the image to be shared.
     */
    fun shareImage(context: Context, imagePath: String?) {
        val uri = Uri.parse(imagePath)
        // Sharing
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "image/jpg"

        try {
            intent.putExtra(Intent.EXTRA_STREAM, uri)
        } catch (e: java.lang.Exception) {
            Toast.makeText(
                context,
                "Failed to share!\nPlease try again.",
                Toast.LENGTH_SHORT
            ).show()
        }
        context.startActivity(Intent.createChooser(intent, "Share via:"))
    }

    /**
     * Helper method for getting the size of an image in byte unit.
     *
     * @param context   The image size in long.
     */
    fun getSize(size: Long): String {
        if (size <= 0) {
            return "0"
        }
        val d = size.toDouble()
        val log10 = (Math.log10(d) / Math.log10(1024.0)).toInt()
        val str = StringBuilder()
        val decimalFormat = DecimalFormat(DECIMAL_FORMAT)
        val power = Math.pow(1024.0, log10.toDouble())
        str.append(decimalFormat.format(d / power))
        str.append(" ")
        str.append(arrayOf("B", "KB", "MB", "GB", "TB")[log10])
        return str.toString()
    }

}

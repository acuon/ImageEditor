package dev.acuon.imageeditor.ui.fragment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import coil.load
import coil.transform.CircleCropTransformation

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import dev.acuon.imageeditor.R
import dev.acuon.imageeditor.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private lateinit var bindingHome: FragmentHomeBinding
    private val CAMERA_REQUEST_CODE = 1
    private val GALLERY_REQUEST_CODE = 2

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingHome = FragmentHomeBinding.inflate(inflater, container, false)
        return bindingHome.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindingHome.fab.setOnClickListener {
            val dialogItem = arrayOf("Take Photo", "Choose Image")

            val optionsDialog = AlertDialog.Builder(requireContext())
                .setTitle("Add Image")
                .setItems(dialogItem) { _, which ->
                    when (which) {
                        0 -> checkCameraPermissions()
                        1 -> checkGalleryPermissions()
                    }
                }
            optionsDialog.show()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun checkGalleryPermissions() {
        Dexter.withContext(activity).withPermission(
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : PermissionListener {
            override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                openGallery()
            }

            override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                Toast.makeText(
                    activity,
                    "You have denied the storage permission to select image",
                    Toast.LENGTH_SHORT
                ).show()
                showRotationalDialogForPermission()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: PermissionRequest?, p1: PermissionToken?
            ) {
                showRotationalDialogForPermission()
            }
        }).onSameThread().check()
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, CAMERA_REQUEST_CODE)
    }

    private fun checkCameraPermissions() {

        Dexter.withContext(activity)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ).withListener(

                object : MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {

                            if (report.areAllPermissionsGranted()) {
                                openCamera()
                            }

                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showRotationalDialogForPermission()
                    }

                }
            ).onSameThread().check()
    }

    private fun showRotationalDialogForPermission() {
        val permissionDeniedDialog = AlertDialog.Builder(requireActivity())
        permissionDeniedDialog.setMessage(
            "It looks like you have turned off permissions required for this feature. It can be enable under App settings!!!"
        )

        permissionDeniedDialog.setPositiveButton("Go TO SETTINGS") { _, _ ->
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", requireActivity().packageName, null)
                intent.data = uri
                startActivity(intent)

            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }

        permissionDeniedDialog.setNegativeButton("CANCEL") { dialog, _ ->
            dialog.dismiss()
        }
        permissionDeniedDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
                    val bitmap = data?.extras?.get("data") as Bitmap
                    //we are using coroutine image loader (coil)
                    bindingHome.imageView.load(bitmap) {
                        crossfade(true)
                        crossfade(1000)
//                        transformations(CircleCropTransformation())
                    }
                }

                GALLERY_REQUEST_CODE -> {
                    bindingHome.imageView.load(data?.data) {
                        crossfade(true)
                        crossfade(1000)
                    }

                }
            }

        }

    }

}
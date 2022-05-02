package dev.acuon.imageeditor.ui.fragment

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.recyclerview.widget.GridLayoutManager
import coil.load
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.theartofdev.edmodo.cropper.CropImage
import dev.acuon.imageeditor.R
import dev.acuon.imageeditor.databinding.FragmentHomeBinding
import dev.acuon.imageeditor.ui.MainActivity
import dev.acuon.imageeditor.ui.adapter.ImageAdapter
import dev.acuon.imageeditor.ui.adapter.OnItemClickListener
import dev.acuon.imageeditor.ui.model.ImageModel
import dev.acuon.imageeditor.utils.*
import dev.acuon.imageeditor.utils.BitmapUtils
import java.io.File

class HomeFragment : Fragment(), OnItemClickListener {

    private lateinit var navController: NavController
    private lateinit var adapter: ImageAdapter

    private var arrayList = ArrayList<ImageModel>()

    private lateinit var bindingHome: FragmentHomeBinding

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

        navController = Navigation.findNavController(view)

        bindingHome.apply {
            fab.setOnClickListener {
                val optionsDialog = AlertDialog.Builder(requireContext())
                    .setTitle(R.string.add_image)
                    .setItems(DIALOG_ITEMS) { _, which ->
                        when (which) {
                            0 -> checkCameraPermissions()
                            1 -> checkGalleryPermissions()
                        }
                    }
                optionsDialog.show()
            }

            val linearLayoutManager = GridLayoutManager(requireContext(), 4)
            recyclerView.layoutManager = linearLayoutManager
            recyclerView.setHasFixedSize(true)

        }
        getImages()
    }

    private fun getImages() {
        arrayList.clear()
        val directory = File(FILE_PATH)
        val files = directory.listFiles()
        if (files != null) {
            for (file in files) {
                if (suffixCheck(file)) {
                    arrayList.add(ImageModel(file.name, file.path, file.length()))
                }
            }
        }
        adapter = ImageAdapter(requireContext(), arrayList, this)
        bindingHome.recyclerView.adapter = adapter
    }

    private fun suffixCheck(file: File): Boolean {
        return (file.path.endsWith(PNG) || file.path.endsWith(JPEG) || file.path.endsWith(JPG))
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = INTENT_IMAGE_TYPE
        intent.putExtra(Intent.EXTRA_MIME_TYPES, MIME_TYPES)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
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
                    R.string.you_have_denied_storage_permission,
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
            R.string.permission_denied_message
        )

        permissionDeniedDialog.setPositiveButton(R.string.go_to_settings) { _, _ ->
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

                    val path = BitmapUtils.saveImage(requireContext(), bitmap)
                    val prev = path
                    path?.let {
                        launchImageCrop(it)
                    }
                    getImages()
                }

                GALLERY_REQUEST_CODE -> {
                    data?.data.let { uri ->
                        launchImageCrop(uri!!)
                    }
                    getImages()
                }

                CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                    val result = CropImage.getActivityResult(data)
                    if (resultCode == Activity.RESULT_OK) {
                        BitmapUtils.saveImageUsingUri(requireContext(), result.uri)
                        getImages()
                    } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                        Log.d("ImageCropping", "onActivityResult: ${result.error}")
                    }
                }
            }
        }
    }

    private fun launchImageCrop(path: String) {
        val uri = Uri.fromFile(File(path))
        CropImage.activity(uri)
            .start(requireActivity(), this)
    }

    private fun launchImageCrop(uri: Uri) {
        CropImage.activity(uri)
            .start(requireActivity(), this)
    }

    override fun onClick(image: ImageModel) {
        val bundle = Bundle()
        bundle.putInt(ENTRY_WAY, GALLERY_REQUEST_CODE)
        bundle.putString(PATH_KEY, image.path)
        bundle.putLong(SIZE_KEY, image.size)
        bundle.putString(TITLE_KEY, image.title)

        navigationHelper(bundle)
    }

    private fun navigationHelper(bundle: Bundle) {
        val navBuilder = NavOptions.Builder()
        navBuilder.setEnterAnim(R.anim.slide_in_right)
//            .setExitAnim(R.anim.slide_in_right)
            .setPopEnterAnim(R.anim.slide_in_left)
//            .setPopExitAnim(R.anim.slide_in_right)
        navController.navigate(
            R.id.action_homeFragment_to_editFragment,
            bundle,
            navBuilder.build()
        )
    }

    private fun toastMessage(str: String) {
        Toast.makeText(requireContext(), str, Toast.LENGTH_SHORT).show()
    }

}
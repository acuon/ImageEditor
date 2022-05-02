package dev.acuon.imageeditor.ui.fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.theartofdev.edmodo.cropper.CropImage
import dev.acuon.imageeditor.R
import dev.acuon.imageeditor.databinding.BottomSheetImageDeleteBinding
import dev.acuon.imageeditor.databinding.BottomSheetImageInfoBinding
import dev.acuon.imageeditor.databinding.FragmentImageViewBinding
import dev.acuon.imageeditor.ui.MainActivity
import dev.acuon.imageeditor.utils.*
import java.io.File

class ImageViewFragment : Fragment() {

    private lateinit var bindingEdit: FragmentImageViewBinding
    private lateinit var path: String
    private lateinit var title: String
    private var size: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getImageDataFromHomeFragment()
    }

    private fun getImageDataFromHomeFragment() {
        arguments?.run {
            path = getString(PATH_KEY)!!
            size = getLong(SIZE_KEY)
            title = getString(TITLE_KEY)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingEdit = FragmentImageViewBinding.inflate(inflater, container, false)
        return bindingEdit.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bindingEdit.apply {
            Glide.with(requireContext()).load(path).placeholder(R.drawable.ic_broken_image)
                .into(imageView)

            imageShare.setOnClickListener {
                BitmapUtils.shareImage(requireContext(), path)
            }

            imageDelete.setOnClickListener {
                openBottomSheetForDelete()
            }

            imageEdit.setOnClickListener {
                launchImageCrop()
            }

            imageInfo.setOnClickListener {
                openBottomSheetForInfo()
            }

            backToHome.setOnClickListener {
                backToHomeFragment()
            }
        }
    }

    private fun backToHomeFragment() {
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
    }

    private fun openBottomSheetForDelete() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_image_delete, null)
        val imageDeleteBinding = BottomSheetImageDeleteBinding.bind(view)
        bottomSheetDialog.apply {
            setContentView(imageDeleteBinding.root)
            setCancelable(true)
            window?.attributes!!.windowAnimations = R.style.DialogAnimation
            show()
        }

        imageDeleteBinding.deleteImageConfirm.setOnClickListener {
            BitmapUtils.deleteImageFile(requireContext(), path).let {
                if (it) {
                    toastMessage(R.string.delete_success)
                    backToHomeFragment()
                } else {
                    toastMessage(R.string.delete_failure)
                }
                bottomSheetDialog.cancel()
            }
        }
        imageDeleteBinding.deleteImageCancel.setOnClickListener {
            bottomSheetDialog.cancel()
        }
    }

    private fun openBottomSheetForInfo() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val view = layoutInflater.inflate(R.layout.bottom_sheet_image_info, null)
        val imageInfoBinding = BottomSheetImageInfoBinding.bind(view)
        imageInfoBinding.apply {
            imageName.text = title
            imageSize.text = BitmapUtils.getSize(size)
            imagePath.text = path
        }
        bottomSheetDialog.apply {
            setContentView(imageInfoBinding.root)
            setCancelable(true)
            window?.attributes!!.windowAnimations = R.style.DialogAnimation
            show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE -> {
                val result = CropImage.getActivityResult(data)
                if (resultCode == Activity.RESULT_OK) {
                    BitmapUtils.saveImageUsingUri(requireContext(), result.uri)
                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Log.d("ImageCropping", "onActivityResult: ${result.error}")
                }
            }
        }
    }

    private fun launchImageCrop() {
        val uri = Uri.fromFile(File(path))
        CropImage.activity(uri)
            .start(requireActivity(), this)
    }

    private fun toastMessage(string: Int) {
        Toast.makeText(requireContext(), string, Toast.LENGTH_SHORT).show()
    }

}
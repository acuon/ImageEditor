package dev.acuon.imageeditor.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import dev.acuon.imageeditor.R
import dev.acuon.imageeditor.databinding.FragmentEditBinding
import dev.acuon.imageeditor.ui.model.ImageModel
import dev.acuon.imageeditor.utils.PATH_KEY

class ImageViewFragment : Fragment() {

    private lateinit var bindingEdit: FragmentEditBinding
    private lateinit var path: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getImageDataFromHomeFragment()
    }

    private fun getImageDataFromHomeFragment() {
        arguments?.run {
            path = getString(PATH_KEY)!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingEdit = FragmentEditBinding.inflate(inflater, container, false)
        return bindingEdit.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Glide.with(requireContext()).load(path).placeholder(R.drawable.ic_broken_image).into(bindingEdit.imageView2)
    }

}
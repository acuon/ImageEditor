package dev.acuon.imageeditor.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import dev.acuon.imageeditor.R
import dev.acuon.imageeditor.databinding.FragmentEditBinding

class EditFragment : Fragment() {

    private lateinit var bindingEdit: FragmentEditBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        bindingEdit = FragmentEditBinding.inflate(inflater, container, false)
        return bindingEdit.root
    }

}
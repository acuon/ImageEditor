package dev.acuon.imageeditor.ui.adapter

import dev.acuon.imageeditor.ui.model.ImageModel

interface OnItemClickListener {
    fun onClick(image: ImageModel)
}
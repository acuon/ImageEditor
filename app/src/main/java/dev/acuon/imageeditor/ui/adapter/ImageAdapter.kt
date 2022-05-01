package dev.acuon.imageeditor.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import dev.acuon.imageeditor.R
import dev.acuon.imageeditor.ui.model.ImageModel
import dev.acuon.imageeditor.utils.DECIMAL_FORMAT
import java.text.DecimalFormat

class ImageAdapter(
    var context: Context,
    var arrayList: ArrayList<ImageModel>,
    private val clickListener: OnItemClickListener
) :
    RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view, clickListener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        return arrayList[position].let {
            holder.setData(it)
        }
    }

    override fun getItemCount(): Int {
        return arrayList.size
    }

    class ViewHolder(itemView: View, var clickListener: OnItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        var imageView: ImageView

        fun setData(image: ImageModel) {
            itemView.apply {
                Glide.with(context).load(image.path).placeholder(R.drawable.ic_broken_image)
                    .into(imageView)
                itemView.setOnClickListener {
                    clickListener.onClick(image)
                }
            }
        }

        init {
            imageView = itemView.findViewById(R.id.list_item_image)
        }
    }

    companion object {
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
}
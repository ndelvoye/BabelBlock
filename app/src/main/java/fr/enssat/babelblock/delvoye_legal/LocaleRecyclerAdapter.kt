package fr.enssat.babelblock.delvoye_legal

import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import fr.enssat.babelblock.delvoye_legal.models.LocaleItem
import java.util.*
import kotlin.collections.ArrayList

class LocaleRecyclerAdapter : RecyclerView.Adapter<LocaleRecyclerAdapter.ViewHolder>() {
    private var localeItemList =  ArrayList<LocaleItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = localeItemList.size

    override fun onBindViewHolder(myViewHolder: ViewHolder, position: Int) {
        val item = localeItemList[position]
        myViewHolder.bind(item)
        myViewHolder.itemView.tag = position
        myViewHolder.deleteButton.setOnClickListener {
            this.localeItemList.removeAt(position)
            notifyItemRemoved(position)
            Log.d("LocaleRecyclerAdapter", "Removed item ${item.language} ($position)")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    /**
     * Add a LocaleItem
     */
    fun addItem(item: LocaleItem, position: Int) {
        Log.d("LocaleRecyclerAdapter", "Added item ${item.language} ($position)")
        this.localeItemList.add(item)
        notifyItemInserted(position)
    }

    fun submitList(localeList: ArrayList<LocaleItem>) {
        localeItemList = localeList
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val languageFlagImg: ImageView = itemView.findViewById(R.id.language_flag_img)
        val chainTranslationText: TextView = itemView.findViewById(R.id.chain_translation_text)
        val editButton: Button = itemView.findViewById(R.id.edit_locale_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_locale_button)

        fun bind(item: LocaleItem) {
            languageFlagImg.setImageResource(stringToFlagImg(item.language.language))
        }

        private fun stringToFlagImg(s: String): Int {
            return when (s) {
                Locale.FRENCH.language -> R.mipmap.france_flag
                Locale.ENGLISH.language -> R.mipmap.united_kingdom_flag
                "es" -> R.mipmap.spain_flag
                Locale.CHINESE.language -> R.mipmap.china_flag
                Locale.ITALIAN.language -> R.mipmap.italy_flag
                Locale.JAPANESE.language -> R.mipmap.japan_flag
                Locale.GERMAN.language -> R.mipmap.germany_flag
                else -> throw Exception("Locale not recognized : $s")
            }
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater
                    .inflate(R.layout.layout_locale_list_item, parent, false)

                return ViewHolder(view)
            }
        }
    }
}
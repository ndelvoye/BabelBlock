package fr.enssat.babelblock.delvoye_legal

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.enssat.babelblock.delvoye_legal.models.LocaleItem
import kotlinx.android.synthetic.main.layout_locale_list_item.view.*
import timber.log.Timber
import java.util.*
import kotlin.collections.ArrayList

class LocaleRecyclerAdapter : RecyclerView.Adapter<LocaleRecyclerAdapter.ViewHolder>() {
    private var localeItemList = ArrayList<LocaleItem>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = localeItemList.size

    // Update ViewHolder with a LocaleItem
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        val item = localeItemList[position]
        viewHolder.bind(item)
        viewHolder.itemView.tag = position
        viewHolder.itemView.chain_translation_text.text = item.translatedText
        viewHolder.deleteButton.setOnClickListener {
            this.localeItemList.removeAt(position)
            notifyItemRemoved(position)
            Timber.d("Removed item ${item.locale} ($position)")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    fun getList(): ArrayList<LocaleItem> {
        return localeItemList
    }

    /**
     * Add a LocaleItem
     */
    fun addItem(item: LocaleItem, position: Int) {
        Timber.d("Added item ${item.locale} (${position})")
        this.localeItemList.add(item)
        notifyItemInserted(position)
    }

    fun submitList(localeList: ArrayList<LocaleItem>) {
        localeItemList = localeList
    }

    class ViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val languageFlagImg: ImageView = itemView.findViewById(R.id.language_flag_img)
        private val chainTranslationText: TextView =
            itemView.findViewById(R.id.chain_translation_text)
        private val editButton: Button = itemView.findViewById(R.id.edit_locale_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_locale_button)

        fun bind(item: LocaleItem) {
            languageFlagImg.setImageResource(stringToFlagImg(item.locale.language))
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
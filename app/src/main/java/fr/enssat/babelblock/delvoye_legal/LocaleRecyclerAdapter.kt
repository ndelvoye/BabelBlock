package fr.enssat.babelblock.delvoye_legal

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import fr.enssat.babelblock.delvoye_legal.models.LocaleItem

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
        val text: TextView = itemView.findViewById(R.id.text_locale)
        val editButton: Button = itemView.findViewById(R.id.edit_locale_button)
        val deleteButton: Button = itemView.findViewById(R.id.delete_locale_button)

        fun bind(item: LocaleItem) {
            val res = itemView.context.resources
            text.text = item.language.displayLanguage
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
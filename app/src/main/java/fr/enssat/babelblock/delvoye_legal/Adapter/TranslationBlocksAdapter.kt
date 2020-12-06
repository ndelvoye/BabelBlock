package fr.enssat.babelblock.delvoye_legal.Adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.list.listItemsSingleChoice
import fr.enssat.babelblock.delvoye_legal.Database.TranslationBlock
import fr.enssat.babelblock.delvoye_legal.MainActivity
import fr.enssat.babelblock.delvoye_legal.R
import fr.enssat.babelblock.delvoye_legal.Tools.BlockService
import fr.enssat.babelblock.delvoye_legal.Utils.LocaleUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TranslationBlocksAdapter :
        ListAdapter<TranslationBlock, TranslationBlocksAdapter.TranslationBlockViewHolder>(
                TranslationBlocksComparator()
        ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TranslationBlockViewHolder {
        return TranslationBlockViewHolder.create(parent)
    }

    override fun onBindViewHolder(holder: TranslationBlockViewHolder, position: Int) {
        val current = getItem(position)
        holder.bind(current)
    }

    class TranslationBlockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val flagImg: ImageView =
                itemView.findViewById(R.id.translation_block_flag)
        private val translation: TextView =
                itemView.findViewById(R.id.translation_block_text)
        private val listenBtn: Button = itemView.findViewById(R.id.listen_button)
        private val editBtn: Button = itemView.findViewById(R.id.edit_button)

        fun bind(translationBlock: TranslationBlock) {
            flagImg.setImageResource(
                    LocaleUtils.stringToFlagInt(
                            translationBlock.language
                    )
            )
            translation.text = translationBlock.translation
            listenBtn.setOnClickListener {
                CoroutineScope(Dispatchers.Default).launch {
                    BlockService(itemView.context as MainActivity)
                            .textToSpeech(LocaleUtils.stringToLocale(translationBlock.language))
                            .speak(itemView.context as MainActivity, translationBlock.translation)
                }
            }
            editBtn.setOnClickListener {
                MaterialDialog(itemView.context).title(R.string.edit_language_dialog_title).show {
                    listItemsSingleChoice(R.array.languages) { _, _, selectedLanguage ->
                        translationBlock.language =
                                LocaleUtils.reduceLanguage(selectedLanguage as String)
                    }
                }
            }
        }

        companion object {
            fun create(parent: ViewGroup): TranslationBlockViewHolder {
                val view: View = LayoutInflater.from(parent.context)
                        .inflate(R.layout.layout_locale_list_item, parent, false)
                return TranslationBlockViewHolder(view)
            }
        }
    }

    class TranslationBlocksComparator : DiffUtil.ItemCallback<TranslationBlock>() {
        override fun areItemsTheSame(
                oldItem: TranslationBlock,
                newItem: TranslationBlock
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
                oldItem: TranslationBlock,
                newItem: TranslationBlock
        ): Boolean {
            return oldItem.position == newItem.position
        }
    }
}
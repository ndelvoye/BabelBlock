package fr.enssat.babelblock.delvoye_legal

import fr.enssat.babelblock.delvoye_legal.models.LocaleItem
import java.util.*
import kotlin.collections.ArrayList

class DataSource {
    companion object {
        fun createDataSet(): ArrayList<LocaleItem>{
            val list = ArrayList<LocaleItem>()
            list.add(LocaleItem(Locale.ENGLISH))
            return list
        }
    }
}
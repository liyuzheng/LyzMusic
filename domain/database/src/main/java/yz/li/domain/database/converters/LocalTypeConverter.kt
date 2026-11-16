package yz.li.domain.database.converters

import androidx.room.TypeConverter
import yz.li.core_tools.exts.toJson
import yz.li.core_tools.exts.toObject
import java.util.Date


/**
 * <pre>
 *     author: dhl
 *     date  : 2020/7/21
 *     desc  :
 * </pre>
 */
open class LocalTypeConverter {
    @TypeConverter
    fun json2StrListEntity(src: String?): MutableList<String>? =
        src.toObject()

    @TypeConverter
    fun strList2Json(data: MutableList<String>?): String? = data?.toJson()

    @TypeConverter
    fun date2Long(date: Date?): Long {
        return date?.time ?: System.currentTimeMillis()
    }

    @TypeConverter
    fun long2Date(time: Long): Date {
        return Date(time)
    }
}
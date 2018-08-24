package org.taskforce.episample.core.util

import com.google.gson.*
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class GsonUtil {


    companion object {
        const val DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        fun getDefaultBuilder(): GsonBuilder {
            return GsonBuilder()
                    .registerTypeAdapter(Date::class.java, DateAdapter())

        }
    }

    class DateAdapter: JsonSerializer<Date>, JsonDeserializer<Date> {

        val dateFormat = SimpleDateFormat(GsonUtil.DATE_FORMAT).apply {
            this.timeZone = TimeZone.getTimeZone("UTC")
        }

        override fun serialize(src: Date?, typeOfSrc: Type?, context: JsonSerializationContext?): JsonElement {
            return JsonPrimitive(dateFormat.format(src))
        }

        override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Date {
            return dateFormat.parse(json?.asString)
        }
    }
}

package org.taskforce.episample.fileImport.processors

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.taskforce.episample.config.landmark.LandmarkTypes
import java.io.File

class LandmarkProcessor {

    fun getLandmarksFromJSON(json: JsonObject) =
        Gson().fromJson<LandmarkTypes>(json, LandmarkTypes::class.java)

    fun getLandmarksFromFile(file: File) =
            Gson().fromJson<LandmarkTypes>(file.bufferedReader(), LandmarkTypes::class.java)

    fun getLandmarksFromTarget(target: String) = getLandmarksFromFile(File(target))
}
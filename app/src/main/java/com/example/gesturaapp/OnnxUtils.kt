package com.example.gesturaapp

import android.content.Context
import java.io.File
import java.io.FileOutputStream

fun loadModelFile(context: Context, modelName: String): File {
    val file = File(context.filesDir, modelName)
    if (!file.exists()) {
        context.assets.open(modelName).use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
    }
    return file
}

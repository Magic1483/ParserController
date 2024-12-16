package com.example.parsercontroller

import android.content.Context
import kotlinx.io.IOException

class FilesHelper(private val ctx: Context) {
    // Reading from assets
    fun readFromAssets( fileName: String): String {
        return this.ctx.assets.open(fileName)
            .bufferedReader()
            .use { it.readText() }
    }

    // List files in an assets subdirectory
    fun listAssetsFiles( path: String): List<String> {
        return try {
            this.ctx.assets.list(path)?.toList() ?: emptyList()
        } catch (e: IOException) {
            emptyList()
        }
    }
}
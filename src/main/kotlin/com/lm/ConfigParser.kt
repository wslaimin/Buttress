package com.lm

import com.google.gson.Gson
import java.io.File
import java.io.FileReader
import java.io.InputStream
import java.io.InputStreamReader

object ConfigParser {
    fun parse(file: File): List<FlatItemNode> {
        val reader = FileReader(file)
        val json = reader.readText()
        reader.close()
        val configModel = Gson().fromJson(json, ConfigModel::class.java)
        val root = ItemNode("options", "label", null, configModel?.nodes)
        return parse(root, 0)
    }

    fun parse(inputStream: InputStream): List<FlatItemNode> {
        val reader = InputStreamReader(inputStream)
        val json = reader.readText()
        reader.close()
        val configModel = Gson().fromJson(json, ConfigModel::class.java)
        Configuration.configModel = configModel
        val root = ItemNode("options", "label", null, configModel?.nodes)
        return parse(root, 0)
    }

    private fun parse(node: ItemNode, deep: Int): List<FlatItemNode> {
        val items = mutableListOf<FlatItemNode>()
        node.nodes?.forEach {
            items.addAll(parse(it, deep + 1))
        }
        items.add(0, FlatItemNode(node.name, node.type, node.dir, deep))
        return items
    }
}

fun main() {
    val str = "/com/addcn/car";
    str.split("/").forEach {
        println(it)
    }

    val str2 = "com/addcn/car";
    str2.split("/").forEach {
        println(it)
    }
}

package com.lm

import com.google.gson.Gson
import java.io.InputStream
import java.io.InputStreamReader

object ConfigParser {
    fun parse(inputStream: InputStream): List<FlatItemNode> {
        return inputStream.use { stream ->
            val reader = InputStreamReader(stream)
            val json = reader.readText()
            reader.close()
            val rootNode = Gson().fromJson(json, ItemNode::class.java)
            val list = ArrayList<FlatItemNode>()
            rootNode.nodes?.forEach {
                var fileHump = it.fileHump ?: rootNode.fileHump ?: false
                var classHump = it.classHump ?: rootNode.classHump ?: false
                list.add(FlatItemNode(it.name, it.type, it.dir, 0, false,fileHump,classHump))
                if(it.type=="item"&&it.nodes!=null){
                    throw RuntimeException("Item node must not hava nodes")
                }else {
                    it.nodes?.forEach { childNode ->
                        fileHump = childNode.fileHump ?: it.fileHump ?: rootNode.fileHump ?: false
                        classHump = childNode.classHump ?: it.classHump ?: rootNode.classHump ?: false
                        list.add(
                            FlatItemNode(
                                childNode.name,
                                childNode.type,
                                childNode.dir,
                                1,
                                false,
                                fileHump,
                                classHump
                            )
                        )
                    }
                }
            }
            list
        }
    }
}

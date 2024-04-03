package com.lm

data class ItemNode(
    val name: String,
    val fileHump: Boolean?,
    val classHump: Boolean?,
    val type: String?,
    val dir: String?,
    val nodes: List<ItemNode>?
)
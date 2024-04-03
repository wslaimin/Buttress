package com.lm

class FlatItemNode(
    val name: String,
    val type: String?,
    val dir: String?,
    val deep: Int,
    var selected: Boolean = false,
    val fileHump: Boolean,
    val classHump: Boolean
)
package com.lm

import com.intellij.openapi.module.Module
import com.intellij.openapi.project.Project

object ArchitectContext {
    lateinit var project: Project
    lateinit var module: Module
}
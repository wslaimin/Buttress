package com.lm

import MainPanel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.awt.Dimension

class CreateAction:AnAction(){
    override fun actionPerformed(e: AnActionEvent) {
        val project=e.project ?: return
        val module = LangDataKeys.MODULE.getData(e.dataContext) ?: return
        ArchitectContext.project=project
        ArchitectContext.module=module
        val directory = when (val navigatable = LangDataKeys.NAVIGATABLE.getData(e.dataContext)) {
            is PsiDirectory -> navigatable
            is PsiFile -> navigatable.containingDirectory
            else -> {
                val root = ModuleRootManager.getInstance(module)
                root.contentRoots
                    .asSequence()
                    .mapNotNull {
                        PsiManager.getInstance(project).findDirectory(it)
                    }.firstOrNull()
            }
        } ?: return

        WriteCommandAction.runWriteCommandAction(ArchitectContext.project){
            val configFile=getTemplateDir()?.findFile("config.json")

            val nodes = if(configFile!=null){
                ConfigParser.parse(configFile.virtualFile.inputStream)
            }else{
                ConfigParser.parse(javaClass.classLoader.getResource("config/config.json").openStream())
            }

            ApplicationManager.getApplication().invokeLater{
                val mainPanel=MainPanel(directory)
                mainPanel.setLocationRelativeTo(null)
                mainPanel.size= Dimension(600,400)
                mainPanel.setList(nodes)
                val language=Configuration.configModel?.language ?: ""
                mainPanel.setLanguage(language)
                mainPanel.isVisible=true
            }
        }
    }
}
package com.lm

import MainPanel
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.awt.Dimension

class CreateAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val module = LangDataKeys.MODULE.getData(e.dataContext) ?: return
        ArchitectContext.project = project
        ArchitectContext.module = module
        ArchitectContext.module.moduleTypeName
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

        if (getTemplateDir()?.findChild("config.json") == null) {
            getModuleDir(ArchitectContext.module)?.let {
                val configFile = createFile(it, "buttress/config.json")
                javaClass.classLoader?.getResource("config/config.json")?.openStream()?.let { stream ->
                    writeToFile(configFile, stream)
                }
                val exampleFile = createFile(it, "buttress/ButtressView.java")
                javaClass.classLoader?.getResource("config/example")?.openStream()?.let { stream ->
                    writeToFile(exampleFile, stream)
                }
            }
        }
        getTemplateDir()?.let {
            it.findChild("config.json")?.inputStream?.let { stream ->
                showPanel(directory, ConfigParser.parse(stream))
            }
        }
    }

    private fun showPanel(directory: PsiDirectory, nodes: List<FlatItemNode>) {
        ApplicationManager.getApplication().invokeLater {
            val mainPanel = MainPanel(directory.virtualFile, nodes)
            mainPanel.setLocationRelativeTo(null)
            mainPanel.size = Dimension(600, 400)
            mainPanel.isVisible = true
        }
    }
}
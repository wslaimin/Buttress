package com.lm

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.module.Module
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.util.Computable
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.InputStream
import java.util.*

fun createFile(dir: VirtualFile, file: String): VirtualFile {
    if (!dir.isDirectory) {
        throw IllegalArgumentException("${dir.canonicalPath} is not a directory")
    } else {
        return ApplicationManager.getApplication().runWriteAction(Computable {
            val parts = file.split("/")
            var lastFile = dir
            parts.forEachIndexed { index, s ->
                if (index != parts.size - 1) {
                    var subDir = lastFile.findChild(s)
                    if (subDir == null || !subDir.isDirectory) {
                        subDir = lastFile.createChildDirectory(null, s)
                    }
                    lastFile = subDir
                } else {
                    var doc = lastFile.findChild(s)
                    if (doc == null || doc.isDirectory) {
                        doc = lastFile.createChildData(null, s)
                    }
                    lastFile = doc
                }
            }
            lastFile
        })
    }
}

fun writeToFile(file: VirtualFile, text: String?) {
    ApplicationManager.getApplication().runWriteAction {
        FileDocumentManager.getInstance().getDocument(file)?.setText(text ?: "")
    }
}

fun writeToFile(file: VirtualFile, inputStream: InputStream) {
    ApplicationManager.getApplication().runWriteAction {
        inputStream.use {
            val scanner = Scanner(it).useDelimiter("\\A");
            if (scanner.hasNext()) {
                VfsUtil.saveText(file, scanner.next())
            }
        }
    }
}

fun readFile(file: VirtualFile): String? {
    return FileDocumentManager.getInstance().getDocument(file)?.text
}

fun getTemplateDir(): VirtualFile? {
    val module = ArchitectContext.module
    return getModuleDir(module)?.findChild("buttress")
}

fun getModuleDir(module: Module): VirtualFile? {
    val contentRoots = module.rootManager.contentRoots.filter { it.isDirectory }
    return contentRoots.find { it.name == module.name } ?: contentRoots.firstOrNull()
}
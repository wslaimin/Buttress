package com.lm

import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.roots.ModuleRootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import java.io.IOException

fun trimToRelativePath(path: String?): String? {
    return if (path == null) {
        null
    } else {
        var route = path
        if (route.startsWith("/")) {
            route = route.replaceFirst("/", "")
        }
        if (route.endsWith("/")) {
            route = route.substring(0, route.length - 1)
        }
        route
    }
}

fun createFile(dir: PsiDirectory, route: String?, name: String, content: String?) {
    val path = trimToRelativePath(route)
    var lastDir = dir
    path?.let { p ->
        val parts = p.split("/")
        parts.forEach {
            var subDir = lastDir.findSubdirectory(it)
            if (subDir == null) {
                subDir = dir.createSubdirectory(it)
            }
            lastDir = subDir
        }
    }
    if (lastDir.findFile(name) != null) {
        throw IOException("$path/$name already exists")
    }
    val file = PsiFileFactory.getInstance(ArchitectContext.project)
        .createFileFromText(FileTypes.PLAIN_TEXT.language, content ?: "")
    file.name = name
    lastDir.add(file)
}

fun writeToFile(file: VirtualFile, text: String?) {
    FileDocumentManager.getInstance().getDocument(file)?.setText(text ?: "")
}

fun readFile(file: VirtualFile): String? {
    return FileDocumentManager.getInstance().getDocument(file)?.text
}

fun getTemplateDir(): PsiDirectory? {
    val project = ArchitectContext.project
    val module = ArchitectContext.module
    var file = ModuleRootManager.getInstance(module).contentRoots[0]
    /*while (file != null && "src" != file.name) {
        file = file.parent
    }
    return if (file != null && "src" == file.name) {
        file = file.parent
        file = file.findChild("template")
        PsiManager.getInstance(project).findDirectory(file)
    } else {
        null
    }*/
    if(file!=null){
        file=file.findChild("template")
    }
    return if(file!=null) PsiManager.getInstance(project).findDirectory(file) else null
}

fun createTemplateDir():PsiDirectory?{
    val project = ArchitectContext.project
    val module = ArchitectContext.module
    var file = ModuleRootManager.getInstance(module).contentRoots[0]
    file?.let{
        file=file.createChildDirectory("","template")
    }
    return if(file!=null) PsiManager.getInstance(project).findDirectory(file) else null
}
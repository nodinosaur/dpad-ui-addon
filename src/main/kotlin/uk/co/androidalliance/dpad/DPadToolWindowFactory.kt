package uk.co.androidalliance.dpad

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class DPadToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val dPadToolWindowContent = DPadToolWindowContent(project)
        val contentFactory = ContentFactory.getInstance()
        val content = contentFactory.createContent(dPadToolWindowContent, "", false)
        content.setDisposer(Disposer.newDisposable().also {
            Disposer.register(it) { dPadToolWindowContent.dispose() }
        })
        toolWindow.contentManager.addContent(content)
    }
}

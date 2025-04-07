package uk.co.androidalliance.dpad

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class DPadToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val dPadToolWindowContent = DPadToolWindowContent(project) // Pass project if needed
        val contentFactory = ContentFactory.getInstance() // Use getInstance() instead of service()
        val content = contentFactory.createContent(dPadToolWindowContent, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
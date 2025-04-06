package uk.co.androidalliance.dpad

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class RemoteControlToolWindowFactory : ToolWindowFactory {

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val remoteControlPanel = RemoteControlPanel(project) // Pass project if needed
        val contentFactory = ContentFactory.getInstance() // Use getInstance() instead of service()
        val content = contentFactory.createContent(remoteControlPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
package liveplugin.implementation.actions.addplugin

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.ui.InputValidatorEx
import com.intellij.openapi.ui.Messages.showInputDialog
import liveplugin.implementation.LivePlugin.Companion.livePluginsById
import liveplugin.implementation.LivePluginPaths.groovyExamplesPath
import liveplugin.implementation.LivePluginPaths.kotlinExamplesPath
import liveplugin.implementation.LivePluginPaths.livePluginsPath
import liveplugin.implementation.common.Icons.newPluginIcon
import liveplugin.implementation.common.IdeUtil
import liveplugin.implementation.common.createFile
import liveplugin.implementation.pluginrunner.groovy.GroovyPluginRunner.Companion.groovyScriptFile
import liveplugin.implementation.pluginrunner.kotlin.KotlinPluginRunner.Companion.kotlinScriptFile
import liveplugin.implementation.readSampleScriptFile
import java.io.IOException

class AddNewGroovyPluginAction : AddNewPluginAction(
    text = "Groovy Plugin",
    description = "Create new Groovy plugin",
    scriptFileName = groovyScriptFile,
    scriptFileText = readSampleScriptFile("$groovyExamplesPath/default-plugin.groovy")
)

class AddNewKotlinPluginAction : AddNewPluginAction(
    text = "Kotlin Plugin",
    description = "Create new Kotlin plugin",
    scriptFileName = kotlinScriptFile,
    scriptFileText = readSampleScriptFile("$kotlinExamplesPath/default-plugin.kts")
)

open class AddNewPluginAction(
    text: String,
    description: String,
    private val scriptFileName: String,
    private val scriptFileText: String
) : AnAction(text, description, newPluginIcon), DumbAware {

    private val log = Logger.getInstance(AddNewPluginAction::class.java)
    private val addNewPluginTitle = "Add $text"

    override fun actionPerformed(event: AnActionEvent) {
        val project = event.project
        val newPluginId = showInputDialog(project, "Enter new plugin name:", addNewPluginTitle, null, "", PluginIdValidator()) ?: return
        try {
            createFile("$livePluginsPath/$newPluginId", scriptFileName, scriptFileText, whenCreated = { virtualFile ->
                if (project != null) FileEditorManager.getInstance(project).openFile(virtualFile, true)
            })
        } catch (e: IOException) {
            if (project != null) IdeUtil.showErrorDialog(project, "Error adding plugin '$newPluginId'", addNewPluginTitle)
            log.error(e)
        }
    }
}

class PluginIdValidator : InputValidatorEx {
    private var errorText: String? = null

    override fun checkInput(pluginId: String) =
        (pluginId !in livePluginsById().keys).also { doesNotExist ->
            errorText = if (doesNotExist) null else "There is already a plugin with this name"
        }

    override fun getErrorText(pluginId: String) = errorText

    override fun canClose(pluginId: String) = true
}

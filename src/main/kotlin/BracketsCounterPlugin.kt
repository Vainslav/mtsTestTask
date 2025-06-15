package pumpkingseeds

import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.bundling.Jar

data class BracketsCount(var parentheses: Int, var square: Int, var curly: Int, var angle: Int)

private fun bracketsCountToString(bracketsCount: BracketsCount): String {
    val sb = StringBuilder()

    if (bracketsCount.parentheses > 0) {
        sb.append("(: ").append(bracketsCount.parentheses).append("\n")
    }

    if (bracketsCount.square > 0) {
        sb.append("[: ").append(bracketsCount.square).append("\n")
    }

    if (bracketsCount.curly > 0) {
        sb.append("{: ").append(bracketsCount.curly).append("\n")
    }

    if (bracketsCount.angle > 0) {
        sb.append("<: ").append(bracketsCount.angle).append("\n")
    }

    return sb.toString()
}

open class BracketsCounterExtension {
    var outputFileName: String = "brackets_count.txt"
}

class BracketsCounterPlugin : Plugin<Project>{
    override fun apply(project: Project) {
        var extension = project.extensions.create("bracketsCounter", BracketsCounterExtension::class.java)

        project.tasks.register("countBrackets", CountBracketsTask::class.java) {
            it.outputFile.set(project.layout.buildDirectory.file(extension.outputFileName))
        }

        project.tasks.named("jar") {
            it.dependsOn("countBrackets")
        }

        project.tasks.named("jar", Jar::class.java).configure { task ->
            task.from(
                project.layout.buildDirectory.file(extension.outputFileName)
            )
        }
    }
}

abstract class CountBracketsTask : DefaultTask() {
    @get:OutputFile
    abstract val outputFile: RegularFileProperty
    @TaskAction
    fun countBrackets() {
        val outputFile = outputFile.get().asFile
        outputFile.delete()
        outputFile.createNewFile()

        val counts: MutableMap<String, BracketsCount> = HashMap()

        countBracketsInFiles(counts)

        outputFile.bufferedWriter().use { out ->
            out.write("Brackets count\n\n")

            var total_parentheses = 0
            var total_curly = 0
            var total_angle = 0
            var total_square = 0

            counts.entries.forEach { (file, bracketsCount) ->
                out.write("==============================\n")
                out.write("$file\n\n")
                out.write(bracketsCountToString(bracketsCount))
                total_curly += bracketsCount.curly
                total_angle += bracketsCount.angle
                total_square += bracketsCount.square
                total_parentheses = bracketsCount.parentheses
                out.write("\n")
            }

            out.write("==============================\n")
            out.write("Total parentheses: $total_parentheses\n")
            out.write("Total square: $total_square\n")
            out.write("Total curly: $total_curly\n")
            out.write("Total angle: $total_angle\n")

            out.write("Total brackets: ${total_parentheses+total_angle+total_square+total_curly}\n")
        }
    }

    private fun countBracketsInFiles(counts: MutableMap<String, BracketsCount>) {
        project.fileTree("src").visit{ file ->
            if (!file.isDirectory) {
                val bracketsCount: BracketsCount = BracketsCount(0,0,0,0)
                for (char in file.file.readText()){
                    if (char == '(') {
                        bracketsCount.parentheses += 1
                    }
                    if (char == '['){
                        bracketsCount.square += 1
                    }
                    if (char == '{') {
                        bracketsCount.curly += 1
                    }
                    if (char == '<') {
                        bracketsCount.angle += 1
                    }
                }
                counts[file.name] = bracketsCount
            }
        }
    }
}


import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class BracketsCounterPluginTest {
    @TempDir
    lateinit var testProjectDir: File

    @BeforeEach
    fun setup(){
        testProjectDir.resolve("settings.gradle.kts").apply {
            writeText("""
                pluginManagement {
                    repositories {
                        mavenLocal()
                        gradlePluginPortal()
                    }
                }
                rootProject.name = "test-project"
            """.trimIndent())
        }

        testProjectDir.resolve("build.gradle.kts").apply {
            writeText("""
                plugins {
                    id("java")
                    id("pumpkingseeds.brackets-counter") version "1.0.0"
                }
                
                repositories {
                    mavenCentral()
                }
            """.trimIndent())
        }
    }

    @Test
    fun test_plugin_applied(){
        testProjectDir.resolve("build.gradle.kts").apply {
            appendText("""
                
                tasks.register("listAllTasks"){
                    doLast{
                        tasks.forEach{ task ->
                            println(task.name)
                        }
                    }
                }
            """.trimIndent())
        }

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("listAllTasks")
            .build()

        assertTrue{result.output.contains("countBrackets")}
    }

    @Test
    fun test_one_file(){
        val src = testProjectDir.resolve("src").apply {mkdir()}
        src.resolve("Test.java").writeText("""
                class Test {
                    public static void main(String[] args) {
                        System.out.println("Hello World!");
                    }
                }
            """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("countBrackets")
            .build()

        assertEquals(result.task(":countBrackets")?.outcome, TaskOutcome.SUCCESS)

        val output = testProjectDir.resolve("build/brackets_count.txt")
        assertTrue(output.exists())
        val contents = output.readLines()
        assertTrue(contents.contains("Test.java"))
        assertTrue(contents.contains("Total parentheses: 2"))
        assertTrue(contents.contains("Total square: 1"))
        assertTrue(contents.contains("Total curly: 2"))
        assertTrue(contents.contains("Total angle: 0"))
        assertTrue(contents.contains("Total brackets: 5"))
    }

    @Test
    fun test_multiple_files_2(){
        val src = testProjectDir.resolve("src").apply {mkdir()}
        src.resolve("File1.java").writeText("""
            public class File1 {
                public static void main(String[] args) {
                    String str = "<Просто строка с треугольниками>"
                }
            }
        """.trimIndent())

        src.resolve("File2.java").writeText("""
            public class File2 {
                public void method(){
                    String str2 = "Просто строка с [] квадратами"
                }
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("countBrackets")
            .build()

        assertEquals(result.task(":countBrackets")?.outcome, TaskOutcome.SUCCESS)

        val output = testProjectDir.resolve("build/brackets_count.txt")
        assertTrue(output.exists())
        val contents = output.readLines()
        assertTrue(contents.contains("File1.java"))
        assertTrue(contents.contains("File2.java"))
        assertTrue(contents.contains("Total parentheses: 2"))
        assertTrue(contents.contains("Total square: 2"))
        assertTrue(contents.contains("Total curly: 4"))
        assertTrue(contents.contains("Total angle: 1"))
        assertTrue(contents.contains("Total brackets: 9"))
    }

    @Test
    fun test_multiple_files_4(){
        val src = testProjectDir.resolve("src").apply {mkdir()}

        src.resolve("File1").writeText("""
            ЭТО ПРОСТО ФАЙЛ БЕЗ СКОБОК
        """.trimIndent())

        src.resolve("config.xml").writeText("""
            <Ya>
                <Lublu>
                    <xml>ПУПУПУ</xml>
                </Lublu>
            </Ya>
        """.trimIndent())

        src.resolve("JavaClass.java").writeText("""
            public class JavaClass{
                public static void main(String[] args) {
                    k = 1;
                    for (int i = 2; i < 10; i++) {
                        k = k * i;
                    }
                    System.out.println(k);
                }
            }
        """.trimIndent())

        src.resolve("data.json").writeText("""
            {
                "name": "Vlad",
                "age": "-1",
                "hobbies": ["None", "null", "undefined"],
                "friend": {
                    {"name": "a", "age": 2},
                    {"name": "b", "age": 3}
                }
            }
        """.trimIndent())

        val result = GradleRunner.create()
            .withProjectDir(testProjectDir)
            .withPluginClasspath()
            .withArguments("countBrackets")
            .build()

        assertEquals(result.task(":countBrackets")?.outcome, TaskOutcome.SUCCESS)
        val output = testProjectDir.resolve("build/brackets_count.txt")
        assertTrue(output.exists())
        val contents = output.readLines()
        assertFalse(contents.contains("File1"))
        assertTrue(contents.contains("config.xml"))
        assertTrue(contents.contains("JavaClass.java"))
        assertTrue(contents.contains("data.json"))
        assertTrue(contents.contains("Total parentheses: 3"))
        assertTrue(contents.contains("Total square: 2"))
        assertTrue(contents.contains("Total curly: 7"))
        assertTrue(contents.contains("Total angle: 7"))
        assertTrue(contents.contains("Total brackets: 19"))
    }
}
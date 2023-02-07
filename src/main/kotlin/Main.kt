import processing.core.PApplet
import processing.data.JSONObject
import java.io.File

fun main() {
    Sketch.run()
}


class Sketch() : PApplet() {
    data class Research(
        var name_eng: String, var name_ru: String, var dependencies: MutableList<String?>
    )

    companion object Companion {
        fun run() {
            val art = Sketch()
            art.runSketch()
        }
    }

    private lateinit var json: JSONObject
    private val researches: MutableList<Research> = mutableListOf()
    private val translatedFolderPath =
        "C:\\Users\\aDragon\\IdeaProjects\\AugmentationResearchParser\\src\\main\\resources\\translates"
    private val outputFilePath = "C:\\Users\\aDragon\\Downloads\\researches.loveRelShadowww"

    override fun setup() {
        val translatedFolder = File(translatedFolderPath)
        val outputFile = File(outputFilePath)
        iterateFromAllFile("C:\\Users\\aDragon\\IdeaProjects\\AugmentationResearchParser\\src\\main\\resources\\researches\\")
        getTranslate(translatedFolder, ".title", ".name")
        letMeOut(outputFile)
    }

    private fun iterateFromAllFile(filePath: String) {
        File(filePath).walkTopDown().forEach {
            if (it.isDirectory) return@forEach
            json = loadJSONObject(it.absoluteFile)
            val entries = parseJSONArray(json.get("entries").toString())

            for (i in 0 until entries.size()) {
                val currentObject = entries.getJSONObject(i)
                researches.add(getResearch(currentObject))
            }
        }
    }

    //    Супер неэффективно. Refactor it later....
    private fun getTranslate(folder: File, extraTitle: String, extraName: String) {
        folder.walkTopDown().forEach { translateFile ->
            if (translateFile.isDirectory) return@forEach
            val translateStrings = translateFile.readLines()
            for (i in 0 until researches.size) for (j in translateStrings.indices) {
                val translateString = translateStrings[j].lowercase()
                val currentResearchTitle = researches[i].name_eng.lowercase().plus(extraTitle)
                val currentResearchName = researches[i].name_eng.lowercase().plus(extraName)
                val dependency = researches[i].dependencies

                translateName(translateString, currentResearchTitle, i)
                if (researches[i].name_ru.isBlank()) translateName(translateString, currentResearchName, i)
                translateDependencies(translateString, dependency, extraTitle, i)
                if (dependency.size > 0 && dependency[0]!!.isBlank()) translateDependencies(
                    translateString, dependency, extraName, i
                )
            }
        }
    }

    private fun letMeOut(out: File) {
        var counter = 1
        if (out.exists()) out.writeText("")
        researches.forEach {
            val ru = it.name_ru
            val eng = it.name_eng
            var dep = "["
            if (it.dependencies.size > 0) {
                it.dependencies.forEach { curDep -> dep += "\"$curDep\", " }
            }
            if (dep.length > 1)
                dep = dep.slice(0 until dep.lastIndexOf(','))
            dep+= "]"
            val outString = "${counter++})\n Ru:\"$ru\"\n Eng:\"$eng\"\n Зависимости:$dep \n\n"
            println(outString)
            out.appendText(outString)
        }
    }

    private fun translateName(tString: String, cRes: String, i: Int) {
        if (tString.contains(cRes)) {
            //     Строка в файле с транслейтом выглядит как "some.some.some.name_eng=name_ru"
            val ru = tString.slice(tString.indexOf('=') + 1 until tString.length).trim()
            researches[i].name_ru = ru
        }
    }

    private fun translateDependencies(tString: String, cDep: MutableList<String?>, extra: String, i: Int) {
        for (j in cDep.indices) {
            val cur = cDep[j]?.lowercase().plus(extra)
            if (tString.contains(cur)) {
                val ru = tString.slice(tString.indexOf('=') + 1 until tString.length)
                cDep[j] = ru.trim()
            }
        }
        researches[i].dependencies = cDep
    }

    private fun getResearch(entry: JSONObject): Research {
        val setToRemove = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '!', '~', '@')
        val key = entry.getString("key").removeAll(setToRemove)
        val parents = entry.getJSONArray("parents")

        val parentsList = mutableListOf<String?>()

        if (parents != null) for (j in 0 until parents.size()) {
            parentsList.add(parents.getString(j).removeAll(setToRemove))
        }

        return Research(key, "", parentsList)
    }

    private fun String.removeAll(charactersToRemove: Set<Char>): String {
        return filterNot { charactersToRemove.contains(it) }
    }
}
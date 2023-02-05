import processing.core.PApplet
import processing.data.JSONObject
import java.io.File

fun main() {
    Sketch.run()
}


class Sketch() : PApplet() {
    data class Research(
        var name_eng: String,
        var name_ru: String,
        var dependencies: MutableList<String?>
    )

    companion object Companion {
        fun run() {
            val art = Sketch()
            art.runSketch()
        }
    }

    private lateinit var json: JSONObject
    private val researches: MutableList<Research> = mutableListOf()

    override fun setup() {
        iterateFromAllFile("C:\\Users\\aDragon\\IdeaProjects\\AugmentationResearchParser\\src\\main\\resources\\researchess\\")
        val translateFile =
            File("C:\\Users\\aDragon\\IdeaProjects\\AugmentationResearchParser\\src\\main\\resources\\ru_ru.txt")
        getTranslate(translateFile, ".title")
        researches.forEach { println(it) }
    }

    private fun iterateFromAllFile(filePath: String) {
        File(filePath).walkTopDown()
            .forEach {
                try {
                    json =
                        loadJSONObject(it.absoluteFile)
                    val entries = parseJSONArray(json.get("entries").toString())

                    for (i in 0 until entries.size()) {
                        val currentObject = entries.getJSONObject(i)
                        researches.add(getResearch(currentObject))
                    }
                } catch (e: java.lang.Exception) {
//                    println(e.message)
                }
            }
    }

//    Супер неэффективно. Refactor it later....
    private fun getTranslate(translateFile: File, extra: String) {
        val translateStrings = translateFile.readLines()
        for (i in 0 until researches.size)
            for (j in translateStrings.indices) {
                val translateString = translateStrings[j].lowercase()
                val currentResearch = researches[i].name_eng.lowercase().plus(extra)
                val dependency = researches[i].dependencies

                translateName(translateString, currentResearch, i)
                translateDependencies(translateString, dependency, extra, i)
            }
    }

    private fun translateName(tString: String, cRes: String, i: Int) {
        if (tString.contains(cRes)) {
 //     Строка в файле с транслейтом выглядит как "some.some.some.name_eng=name_ru"
            val ru = tString.slice(tString.indexOf('=') + 1 until tString.length)
            researches[i].name_ru = ru
        }
    }

    private fun translateDependencies(tString: String, cDep: MutableList<String?>, extra: String, i: Int) {
        for (j in cDep.indices) {
            val cur = cDep[j]?.lowercase().plus(extra)
            if (tString.contains(cur)) {
                val ru = tString.slice(tString.indexOf('=') + 1 until tString.length)
                cDep[j] = ru
            }
        }
        researches[i].dependencies = cDep
    }

    private fun getResearch(entry: JSONObject): Research {
        val setToRemove = setOf('1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '!', '~', '@')
        val key = entry.getString("key").removeAll(setToRemove)
        val parents = entry.getJSONArray("parents")

        val parentsList = mutableListOf<String?>()

        if (parents != null)
            for (j in 0 until parents.size()) {
                parentsList.add(parents.getString(j).removeAll(setToRemove))
            }

        return Research(key, "", parentsList)
    }

    private fun String.removeAll(charactersToRemove: Set<Char>): String {
        return filterNot { charactersToRemove.contains(it) }
    }
}
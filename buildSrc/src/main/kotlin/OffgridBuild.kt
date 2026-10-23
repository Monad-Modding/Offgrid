import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType
import java.io.File

val Project.stonecutterBuild: StonecutterBuildExtension
    get() = extensions.getByType()

val Project.mcVersion: String
    get() = stonecutterBuild.current.version

val Project.platform: String
    get() = requireNotNull(parent) { "$path is not a Stonecutter node" }.name

fun Project.setting(key: String): String =
    requireNotNull(findProperty(key)?.toString()) { "Missing Gradle property '$key'" }

fun Project.mod(key: String): String = setting("mod.$key")

fun Project.versioned(key: String): String = setting("$mcVersion.$key")

fun Project.versionedOrNull(key: String): String? = findProperty("$mcVersion.$key")?.toString()

fun Project.integration(key: String, loader: String = platform): String? = versionedOrNull("$key.$loader") ?: versionedOrNull(key)

val Project.javaVersion: Int
    get() = versioned("java").toInt()

val Project.generatedResources: File
    get() = rootProject.file("common/versions/${stonecutterBuild.current.project}/src/generated")

val Project.accessWidener: File
    get() = rootProject.file("common/accesswideners/$mcVersion.accesswidener")

val Project.additionalVersions: List<String>
    get() = versionedOrNull("publish.additional_versions")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() } ?: emptyList()

fun Project.mcAtLeast(version: String): Boolean = stonecutterBuild.eval(mcVersion, ">=$version")

fun Project.replaceSince(version: String, old: String, new: String) {
    stonecutterBuild.replacements.regex(mcAtLeast(version)) {
        replace(wordPattern(old), literalReplacement(new), wordPattern(new), literalReplacement(old))
    }
}

private fun wordPattern(text: String): String {
    val start = if (Character.isJavaIdentifierPart(text.first())) "\\b" else ""
    val end = if (Character.isJavaIdentifierPart(text.last())) "\\b" else ""
    return start + Regex.escape(text) + end
}

private fun literalReplacement(text: String): String = text.replace("\\", "\\\\").replace("$", "\\$")

private val singularDataFolders: List<String> = listOf("loot_table", "recipe", "advancement", "tags/item", "tags/block", "tags/entity_type", "tags/fluid", "tags/game_event")

fun pluralDataPath(path: String): String {
    for (folder in singularDataFolders) {
        val pattern = Regex("^(data/[^/]+/)${Regex.escape(folder)}/")
        if (pattern.containsMatchIn(path)) return path.replaceFirst(pattern, "$1${folder}s/")
    }
    return path
}

private val mixinLists: List<String> = listOf("mixins", "client", "server")

@Suppress("UNCHECKED_CAST")
val Project.refmap: String
    get() = "${mod("id")}.refmap.json"

fun pruneMixinConfig(config: File, classesDirs: Iterable<File>, refmap: String?) {
    val json = JsonSlurper().parse(config) as MutableMap<String, Any?>
    val packagePath = (json["package"] as String).replace('.', '/')
    for (list in mixinLists) {
        val entries = json[list] as? List<String> ?: continue
        json[list] = entries.filter { entry ->
            val classFile = "$packagePath/${entry.replace('.', '/')}.class"
            classesDirs.any { File(it, classFile).isFile }
        }
    }
    if (refmap != null) {
        json["refmap"] = refmap
    }
    config.writeText(JsonOutput.prettyPrint(JsonOutput.toJson(json)) + "\n")
}

data class OptionalMod(val id: String, val slug: String, val minimum: String, val below: String, val clientOnly: Boolean)

val Project.optionalMods: List<OptionalMod>
    get() = versionedOrNull("optional.$platform")?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }?.map { entry ->
        val fields = entry.split(Regex("\\s+"))
        require(fields.size == 4 || fields.size == 5 && fields[4] == "client") { "Optional mod entry '$entry' must be '<mod id> <slug> <minimum> <below> [client]'" }
        OptionalMod(fields[0], fields[1], fields[2], fields[3], fields.size == 5)
    } ?: emptyList()

fun Project.optionalDependencies(modId: String): String = optionalMods.joinToString("") { mod ->
    val requirement = if (platform == "forge") "mandatory = false" else "type = \"optional\""
    "\n[[dependencies.$modId]]\nmodId = \"${mod.id}\"\n$requirement\nversionRange = \"[${mod.minimum},${mod.below})\"\nordering = \"NONE\"\nside = \"${if (mod.clientOnly) "CLIENT" else "BOTH"}\"\n"
}

val Project.fabricBreaks: String
    get() = JsonOutput.toJson(optionalMods.associate { mod -> mod.id to listOf("<${mod.minimum}", ">=${mod.below}") })

val commonContent: org.gradle.api.attributes.Attribute<String> =
    org.gradle.api.attributes.Attribute.of("dev.tazer.offgrid.common", String::class.java)

const val COMMON_CATEGORY: String = "offgrid-common"

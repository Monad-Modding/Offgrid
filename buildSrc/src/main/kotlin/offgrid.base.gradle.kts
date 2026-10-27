plugins {
    java
}

group = "${mod("group")}.$platform"
version = "${mod("version")}+$mcVersion"

base {
    archivesName = "${mod("id")}-$platform"
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(javaVersion)
    withSourcesJar()
}

repositories {
    mavenCentral()
    exclusiveContent {
        forRepository {
            maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }
        }
        filter { includeGroup("org.parchmentmc.data") }
    }
    exclusiveContent {
        forRepository {
            maven("https://maven.fabricmc.net") { name = "Fabric" }
        }
        filter { includeGroupAndSubgroups("net.fabricmc") }
    }
    exclusiveContent {
        forRepository {
            maven("https://api.modrinth.com/maven") { name = "Modrinth" }
        }
        filter { includeGroup("maven.modrinth") }
    }
    exclusiveContent {
        forRepository {
            maven("https://repo.sleeping.town") { name = "Sleeping Town" }
        }
        filter { includeGroup("folk.sisby") }
    }
    exclusiveContent {
        forRepository {
            maven("https://maven.ryanhcode.dev/releases") { name = "RyanHCode" }
        }
        filter { includeGroupAndSubgroups("dev.ryanhcode") }
    }
    exclusiveContent {
        forRepository {
            maven("https://maven.caffeinemc.net/releases") { name = "CaffeineMC" }
        }
        filter { includeGroup("net.caffeinemc") }
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = javaVersion
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "5000"))
}

replaceSince("1.21.11", "ResourceLocation", "Identifier")
replaceSince("26", "FabricDataOutput", "FabricPackOutput")
replaceSince("26", "ServerWorldEvents", "ServerLevelEvents")
replaceSince("26", "END_WORLD_TICK", "END_LEVEL_TICK")
replaceSince("26", "player.canInteractWithBlock", "player.isWithinBlockInteractionRange")
replaceSince("26", "player.serverLevel()", "player.level()")
replaceSince("26", "ChunkPos.asLong", "ChunkPos.pack")
replaceSince("26", "getPos().toLong()", "getPos().pack()")
replaceSince("26", "setUnsaved(true)", "markUnsaved()")
replaceSince("26.2", "net.minecraft.advancements.CriteriaTriggers", "net.minecraft.advancements.triggers.CriteriaTriggers")
replaceSince("26.2", "getMainCamera()", "mainCamera()")
replaceSince("26.2", "minecraft.setScreen(", "minecraft.gui.setScreen(")
replaceSince("26.3", "RedStoneWireBlock", "RedstoneWireBlock")
replaceSince("26.3", "PushReaction.BLOCK", "PushReaction.IMMOVEABLE")
replaceSince("26.3", "LevelEvent.PARTICLES_AND_SOUND_WAX_ON", "LevelEvent.PARTICLES_WAX_ON")

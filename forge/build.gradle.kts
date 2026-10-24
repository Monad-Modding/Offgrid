plugins {
    id("offgrid.loader")
    id("net.neoforged.moddev.legacyforge")
}

legacyForge {
    version = versioned("forge")
    validateAccessTransformers = true
    accessTransformers.files.setFrom(rootProject.file("forge/src/main/resources/META-INF/accesstransformer.cfg"))
    parchment {
        minecraftVersion = mcVersion
        mappingsVersion = versioned("parchment")
    }
    mods {
        register(mod("id")) {
            sourceSet(sourceSets.main.get())
        }
    }
    runs {
        register("client") {
            client()
        }
        register("server") {
            server()
        }
    }
}

mixin {
    add(sourceSets.main.get(), refmap)
    config("${mod("id")}.mixins.json")
    config("${mod("id")}.forge.mixins.json")
}

dependencies {
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")
    annotationProcessor("io.github.llamalad7:mixinextras-common:${setting("mixinextras")}")
    compileOnly("io.github.llamalad7:mixinextras-common:${setting("mixinextras")}")
    implementation("io.github.llamalad7:mixinextras-forge:${setting("mixinextras")}")
    jarJar("io.github.llamalad7:mixinextras-forge:${setting("mixinextras")}")
    implementation("folk.sisby:kaleido-config:${setting("kaleido")}")
    jarJar("folk.sisby:kaleido-config:${setting("kaleido")}")
    "additionalRuntimeClasspath"("folk.sisby:kaleido-config:${setting("kaleido")}")
    integration("clutternomore")?.let { modCompileOnly("maven.modrinth:clutternomore:$it") { isTransitive = false } }
    integration("gallery")?.let { modCompileOnly("maven.modrinth:gallery:$it") { isTransitive = false } }
    integration("create")?.let { modCompileOnly("maven.modrinth:create:$it") { isTransitive = false } }
    integration("resourcefullib")?.let { modCompileOnly("maven.modrinth:resourceful-lib:$it") { isTransitive = false } }
    integration("embeddium")?.let { modCompileOnly("maven.modrinth:embeddium:$it") { isTransitive = false } }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes("MixinConfigs" to "${mod("id")}.mixins.json,${mod("id")}.forge.mixins.json")
    }
}

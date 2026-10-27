plugins {
    id("offgrid.loader")
    id("net.neoforged.moddev")
}

neoForge {
    version = versioned("neoforge")
    validateAccessTransformers = true
    accessTransformers.files.setFrom(rootProject.file("neoforge/src/main/resources/META-INF/accesstransformer.cfg"))
    versionedOrNull("parchment")?.let { mappings ->
        parchment {
            minecraftVersion = mcVersion
            mappingsVersion = mappings
        }
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

dependencies {
    implementation("folk.sisby:kaleido-config:${setting("kaleido")}")
    jarJar("folk.sisby:kaleido-config:${setting("kaleido")}")
    if (!mcAtLeast("1.21.9")) {
        "additionalRuntimeClasspath"("folk.sisby:kaleido-config:${setting("kaleido")}")
    }
    integration("clutternomore")?.let { compileOnly("maven.modrinth:clutternomore:$it") { isTransitive = false } }
    integration("sable")?.let { compileOnly("dev.ryanhcode.sable:sable-neoforge-$mcVersion:$it") { isTransitive = false } }
    integration("sable_companion")?.let { compileOnly("dev.ryanhcode.sable-companion:sable-companion-common-$mcVersion:$it") { isTransitive = false } }
    integration("sodium")?.let { compileOnly("net.caffeinemc:sodium-neoforge-mod:$it") { isTransitive = false } }
    integration("gallery")?.let { compileOnly("maven.modrinth:gallery:$it") { isTransitive = false } }
    integration("create")?.let { compileOnly("maven.modrinth:create:$it") { isTransitive = false } }
    integration("quark")?.let { compileOnly("maven.modrinth:quark:$it") { isTransitive = false } }
    integration("zeta")?.let { compileOnly("maven.modrinth:zeta:$it") { isTransitive = false } }
    integration("additionalplacements")?.let { compileOnly("maven.modrinth:additional-placements:$it") { isTransitive = false } }
    integration("resourcefullib")?.let { compileOnly("maven.modrinth:resourceful-lib:$it") { isTransitive = false } }
    integration("lithium")?.let { compileOnly("maven.modrinth:lithium:$it") { isTransitive = false } }
}

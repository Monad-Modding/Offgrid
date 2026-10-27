plugins {
    id("offgrid.loader")
    id("net.fabricmc.fabric-loom-remap")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$mcVersion:${versioned("parchment")}@zip")
    })
    modImplementation("net.fabricmc:fabric-loader:${setting("fabric.loader")}")
    modImplementation("net.fabricmc.fabric-api:fabric-api:${versioned("fabric_api")}")
    implementation("folk.sisby:kaleido-config:${setting("kaleido")}")
    include("folk.sisby:kaleido-config:${setting("kaleido")}")
    integration("clutternomore")?.let { modCompileOnly("maven.modrinth:clutternomore:$it") { isTransitive = false } }
    integration("snowrealmagic")?.let { modCompileOnly("maven.modrinth:snow-real-magic:$it") { isTransitive = false } }
    integration("kiwi")?.let { modCompileOnly("maven.modrinth:kiwi:$it") { isTransitive = false } }
    integration("sodium")?.let { modCompileOnly("maven.modrinth:sodium:$it") { isTransitive = false } }
    integration("resourcefullib")?.let { modCompileOnly("maven.modrinth:resourceful-lib:$it") { isTransitive = false } }
    integration("lithium")?.let { modCompileOnly("maven.modrinth:lithium:$it") { isTransitive = false } }
    integration("create")?.let { modCompileOnly("maven.modrinth:create-fabric:$it") { isTransitive = false } } ?: integration("create", "neoforge")?.let { compileOnly("maven.modrinth:create:$it") { isTransitive = false } }
    integration("sable")?.let { modCompileOnly("dev.ryanhcode.sable:sable-fabric-$mcVersion:$it") { isTransitive = false } }
    integration("sable_companion")?.let { compileOnly("dev.ryanhcode.sable-companion:sable-companion-common-$mcVersion:$it") { isTransitive = false } }
}

loom {
    accessWidenerPath = accessWidener
    runs {
        register("data") {
            inherit(getByName("server"))
            name("Data Generation")
            vmArg("-Dfabric-api.datagen")
            vmArg("-Dfabric-api.datagen.output-dir=${generatedResources.absolutePath}")
            vmArg("-Dfabric-api.datagen.modid=${mod("id")}")
            runDir("build/datagen")
        }
    }
}

tasks.named<ProcessResources>("processResources") {
    from(accessWidener) {
        rename { "${mod("id")}.accesswidener" }
    }
}

plugins {
    id("offgrid.loader")
    id("net.fabricmc.fabric-loom")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    implementation("net.fabricmc:fabric-loader:${setting("fabric.loader")}")
    implementation("net.fabricmc.fabric-api:fabric-api:${versioned("fabric_api")}")
    implementation("folk.sisby:kaleido-config:${setting("kaleido")}")
    include("folk.sisby:kaleido-config:${setting("kaleido")}")
    integration("clutternomore")?.let { compileOnly("maven.modrinth:clutternomore:$it") { isTransitive = false } }
    integration("sodium")?.let { compileOnly("net.caffeinemc:sodium-fabric:$it") { isTransitive = false } }
    integration("resourcefullib")?.let { compileOnly("maven.modrinth:resourceful-lib:$it") { isTransitive = false } }
    integration("lithium")?.let { compileOnly("maven.modrinth:lithium:$it") { isTransitive = false } }
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

plugins {
    id("offgrid.common")
    id("net.fabricmc.fabric-loom-remap")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$mcVersion:${versioned("parchment")}@zip")
    })
    modCompileOnly("net.fabricmc:fabric-loader:${setting("fabric.loader")}")
    compileOnly("folk.sisby:kaleido-config:${setting("kaleido")}")
    integration("clutternomore", "fabric")?.let { modCompileOnly("maven.modrinth:clutternomore:$it") { isTransitive = false } }
    integration("sodium", "fabric")?.let { modCompileOnly("maven.modrinth:sodium:$it") { isTransitive = false } }
    integration("resourcefullib", "fabric")?.let { modCompileOnly("maven.modrinth:resourceful-lib:$it") { isTransitive = false } }
    integration("lithium", "fabric")?.let { modCompileOnly("maven.modrinth:lithium:$it") { isTransitive = false } }
    integration("create", "fabric")?.let { modCompileOnly("maven.modrinth:create-fabric:$it") { isTransitive = false } } ?: integration("create", "neoforge")?.let { compileOnly("maven.modrinth:create:$it") { isTransitive = false } }
    integration("sable")?.let { compileOnly("dev.ryanhcode.sable:sable-common-$mcVersion:$it") { isTransitive = false } }
    integration("sable_companion")?.let { compileOnly("dev.ryanhcode.sable-companion:sable-companion-common-$mcVersion:$it") { isTransitive = false } }
}

loom {
    accessWidenerPath = accessWidener
}

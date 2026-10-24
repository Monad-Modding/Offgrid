plugins {
    id("offgrid.common")
    id("net.fabricmc.fabric-loom")
}

dependencies {
    minecraft("com.mojang:minecraft:$mcVersion")
    compileOnly("net.fabricmc:fabric-loader:${setting("fabric.loader")}")
    compileOnly("folk.sisby:kaleido-config:${setting("kaleido")}")
    integration("clutternomore", "fabric")?.let { compileOnly("maven.modrinth:clutternomore:$it") { isTransitive = false } }
    integration("sodium")?.let { compileOnly("net.caffeinemc:sodium-fabric:$it") { isTransitive = false } }
    integration("resourcefullib", "fabric")?.let { compileOnly("maven.modrinth:resourceful-lib:$it") { isTransitive = false } }
    integration("lithium", "fabric")?.let { compileOnly("maven.modrinth:lithium:$it") { isTransitive = false } }
}

loom {
    accessWidenerPath = accessWidener
}

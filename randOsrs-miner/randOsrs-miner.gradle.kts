version = "0.0.8"

project.extra["PluginName"] = "-randOsrs Miner"
project.extra["PluginDescription"] = "Sweet mining script that will switch pickaxes and ores/mining areas as you level."

tasks {
    jar {
        manifest {
            attributes(mapOf(
                "Plugin-Version" to project.version,
                "Plugin-Id" to nameToId(project.extra["PluginName"] as String),
                "Plugin-Provider" to project.extra["PluginProvider"],
                "Plugin-Description" to project.extra["PluginDescription"],
                "Plugin-License" to project.extra["PluginLicense"]
            ))
        }
    }
}

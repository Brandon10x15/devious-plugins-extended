version = "0.0.8"

project.extra["PluginName"] = "-randOsrs Fighter"
project.extra["PluginDescription"] = "Simple fighter, supports eating, looting, alching, prayer potions, prayer flicking."

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

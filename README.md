# Rifted Carpet

Rift version of Carpet Mod for Minecraft 1.13.2 Client
Based on [Fabric Carpet](https://github.com/gnembon/fabric-carpet) by [gnembon](https://github.com/gnembon)

Carpet Mod is a mod for vanilla Minecraft that allows you to take full control of what matters from a technical perspective of the game.

## Modding RiftedCarpet

To help develop this mod you need:

- `git clone https://github.com/Kahzerx/RiftedCarpet.git`
- `cd RiftedCarpet`
- `Place the rift_libs folder on that directory (http://maruohon.kapsi.fi/minecraft/rift_1.13.2_with_libs.zip)`
- `gradlew setupDevWorkspace`

Open the project with eclipse/idea and on `External libraries` -> `start` -> `net.minecraftforge.gradle` -> and run `GradleStart`.

Set `--tweakClass org.dimdev.riftloader.launch.RiftLoaderClientTweaker` as program arguments.

Java `1.8` and `classpath module` -> `RiftedCarpet.main`.

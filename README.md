# MammaMia Server Plugin
The Mamma Mia server controller plugin.

### Requirements
- Bukkit/Spigot/Paper 1.16.5 Server
- [Multiverse Core](https://dev.bukkit.org/projects/multiverse-core) (4.2.2)
- [Multiverse Inventories](https://dev.bukkit.org/projects/multiverse-inventories) (4.2.1)
- [Multiverse NetherPortals](https://dev.bukkit.org/projects/multiverse-netherportals/) (4.2.1)

Version in parentheses is the last known working version.

### Plugin features
- Manhunt game
- Loading screen
- Offline mode support
- LabyMod integration

### Manhunt features
- On-the-fly game regeneration
- Discord Bot integration
- Un-droppable compass
- Game automatically resumes after server shutdown
- Shitpost mode

### Plugin installation
Make sure your server is not running while installing.\
Extract the plugin package to the "plugins" folder of the server.\
Add the following permissions to permissions.yml
```yml
server.default:
  default: true
  children:
    mv.bypass.gamemode.manhunt: true
    mv.bypass.gamemode.manhunt_nether: true
    mv.bypass.gamemode.manhunt_the_end: true
    mm.mh.new: false
    mm.mh.start: false
    mm.mh.resume: false
    mm.mh.stop: false
    mm.mh.join: true
    mm.mh.leave: true
    mm.mh.set: false
    mm.mh.reloadtracks: false
    mm.mh.botvc: false
server.op:
  default: op
  children:
    mm.mh.new: true
    mm.mh.start: true
    mm.mh.resume: true
    mm.mh.stop: true
    mm.mh.set: true
    mm.mh.cockhunt: true
    mm.mh.reloadtracks: true
    mm.mh.botvc: true
```
You may change this scheme if you use any other permissions plugin or structure.\
The `mv.bypass.gamemode.manhunt*` permissions are required for all players, otherwise Multiverse will conflict with the plugin's own gamemode management.

To setup the loading screen, create an anchor with multiverse named `loading_screen`.\
You can use the command `mvanchor loading_screen` while facing where you want the loading screen to be at.\
After that, create an invisible armor stand for where the player will be facing the loading screen with the name `load_spect`.\
You can use the command `summon minecraft:armor_stand ~ ~ ~ {CustomName:"load_spect",Invisible:1b}`.

Example:
![load_spect](https://raw.githubusercontent.com/TheGameratorT/MammaMia-Server-Plugin/main/repo/load_spect.jpg)

Make sure "allow-end" is true in "bukkit.yml" and "allow-nether" is true in "server.properties".

To setup the manhunt worlds run the following commands: \
`mvcreate manhunt normal`\
`mvcreate manhunt_nether nether`\
`mvcreate manhunt_the_end end`

`mvinv group`\
`create`\
`manhunt`\
`manhunt`\
`manhunt_nether`\
`manhunt_the_end`\
`@`\
`all`\
`@`

`mvnp link nether manhunt manhunt_nether`\
`mvnp link end manhunt manhunt_the_end`

You might want to mess with the `config.yml` file to enable certain features.\
Everything should now be ready to play!

### Manhunt command reference
Command prefix `manhunt`\
Usage example: `manhunt start`

| Command             | Permission         | Description                                     |
|---------------------|--------------------|-------------------------------------------------|
| new                 | mm.mh.new          | Regenerates the manhunt world                   |
| start               | mm.mh.start        | Starts the manhunt (clears inventory and heals) |
| resume              | mm.mh.resume       | Resumes the manhunt                             |
| stop                | mm.mh.stop         | Stops the manhunt                               |
| join <team>         | mm.mh.join         | Makes you join a team                           |
| leave               | mm.mh.leave        | Makes you leave the manhunt                     |
| set <player> <team> | mm.mh.set          | Sets the team of a player                       |
| cockhunt            | mm.mh.cockhunt     | Enables shitpost mode                           |
| reloadtracks        | mm.mh.reloadtracks | Reloads all of the music tracks                 |
| botvc               | mm.mh.botvc        | Makes the bot re-join voice chat                |

Available manhunt teams:
 - none
 - hunters
 - runners
 - spectators

### Credits
 - Plugin by TheGameratorT
 - [LavaPlayer](https://github.com/sedmelluq/lavaplayer) by sedmelluq

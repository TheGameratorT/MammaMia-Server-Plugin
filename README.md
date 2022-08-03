# MammaMia Server Plugin
The Mamma Mia server controller plugin.

### Requirements
- Bukkit/Spigot/Paper 1.19.1 Server
- [Multiverse Core](https://dev.bukkit.org/projects/multiverse-core) (4.3.2-SNAPSHOT)
- [Multiverse Inventories](https://dev.bukkit.org/projects/multiverse-inventories) (4.2.3)
- [Multiverse NetherPortals](https://dev.bukkit.org/projects/multiverse-netherportals/) (4.2.2)
- [Fast Async World Edit](https://intellectualsites.github.io/download/fawe.html) (2.4.3-SNAPSHOT-254)
- [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/) (5.0.0-SNAPSHOT)

Version in parentheses is the last known working version.

### Plugin features
- Manhunt game
- PvP game
- Kit system
- Loading screen
- Offline mode support

### Manhunt features
- On-the-fly game regeneration
- Discord Bot integration
- Un-droppable compass
- Game automatically resumes after server shutdown
- Shitpost mode
- Disables advancements for spectators

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
    mm.mh.cockhunt: false
    mm.mh.debug: false
    mm.mh.menu: true
server.op:
  default: op
  children:
    mm.mh.new: true
    mm.mh.start: true
    mm.mh.resume: true
    mm.mh.stop: true
    mm.mh.set: true
    mm.mh.cockhunt: true
    mm.mh.debug: true
    mm.mh.menu: true
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

### Main command reference
Command prefix `mammamia`\
Usage example: `mammamia menu`

| Command           | Permission | Description                |
|-------------------|------------|----------------------------|
| menu              | mm.menu    | Opens the Mamma Mia menu   |
| respawn \<player> | mm.respawn | Forces a player to respawn |

### Manhunt command reference
Command prefix `manhunt`\
Usage example: `manhunt start`

| Command               | Permission     | Description                                     |
|-----------------------|----------------|-------------------------------------------------|
| menu                  | mm.mh.menu     | Opens the manhunt menu                          |
| new                   | mm.mh.new      | Regenerates the manhunt world                   |
| start                 | mm.mh.start    | Starts the manhunt (clears inventory and heals) |
| resume                | mm.mh.resume   | Resumes the manhunt                             |
| stop                  | mm.mh.stop     | Stops the manhunt                               |
| join \<team>          | mm.mh.join     | Makes you join a team                           |
| leave                 | mm.mh.leave    | Makes you leave the manhunt                     |
| set \<player> \<team> | mm.mh.set      | Sets the team of a player                       |
| cockhunt              | mm.mh.cockhunt | Enables shitpost mode                           |
| debug                 | mm.mh.debug    | Allows for force starting the manhunt           |

Available manhunt teams:
 - none
 - hunters
 - runners
 - spectators

### Discord command reference
Command prefix `discord`\
Usage example: `discord joinVC`

| Command              | Permission          | Description                                       |
|----------------------|---------------------|---------------------------------------------------|
| joinVC               | mm.discord.joinVC   | Forces the discord bot to join the voice channel  |
| leaveVC              | mm.discord.leaveVC  | Forces the discord bot to leave the voice channel |
| startup              | mm.discord.startup  | Connects the bot client if it's shutdown          |
| shutdown             | mm.discord.shutdown | Disconnects the discord bot client                |
| enable               | mm.discord.enable   | Same as startup but applies to config             |
| disable              | mm.discord.disable  | Same as shutdown but applies to config            |
| set \<prop> \<value> | mm.discord.set      | Sets the value of a bot property                  |

Available discord bot properties:
 - botToken (string)
 - botStatus (string)
 - guildID (long)
 - voiceChannelID (long)

### Track command reference
Command prefix `track`\
Usage example: `track play mh_start 0`

| Command                          | Permission      | Description                             |
|----------------------------------|-----------------|-----------------------------------------|
| play \<trackName> \<startOffset> | mm.track.play   | Starts playing an audio track           |
| stop                             | mm.track.stop   | Stops the currently playing audio track |
| reload                           | mm.track.reload | Reloads all the audio tracks            |

### PvP command reference
Command prefix `pvp`\
Usage example: `pvp startDuel 0 Player1 Player2`

| Command                                      | Permission         | Description                                    |
|----------------------------------------------|--------------------|------------------------------------------------|
| menu                                         | mm.pvp.menu        | Opens the PvP menu                             |
| startDuel \<arenaID> \<player1> \<player2>   | mm.pvp.startDuel   | Starts a PvP duel on the specified arena       |
| startSpleef \<arenaID> \<player1> \<player2> | mm.pvp.startSpleef | Starts a spleef duel on the specified arena    |
| removeGame \<gameID>                         | mm.pvp.removeGame  | Forcefully removes a PvP game instancce        |
| listGames                                    | mm.pvp.listGames   | Lists all PvP game instances and their players |

### Kit command reference
Command prefix `kit`\
Usage example: `kit claim manhunt`

| Command               | Permission         | Description                                                    |
|-----------------------|--------------------|----------------------------------------------------------------|
| menu                  | mm.kit.menu        | Opens the kit menu                                             |
| list                  | mm.kit.list        | Lists all available kits                                       |
| claim \<kit>          | mm.kit.claim       | Gives the specified kit to the player                          |
| give \<player> \<kit> | mm.kit.give        | Gives the specified kit to the specified player                |
| create \<kit>         | mm.kit.create      | Creates a new kit based on the player inventory                |
| remove \<kit>         | mm.kit.remove      | Deletes the specified kit                                      |
| seticon \<kit>        | mm.kit.seticon     | Changes the kit icon to the item being held                    |
| rename \<kit> \<name> | mm.kit.rename      | Renames the specified kit                                      |
| update \<kit>         | mm.kit.update      | Matches the specified kit's contents with the player inventory |
| reload                | mm.kit.reload      | Reloads all kits                                               |

### Credits
 - Plugin by TheGameratorT
 - [LavaPlayer](https://github.com/sedmelluq/lavaplayer) by sedmelluq

# LPPP Homes mod

A /home mod developed for use on my private long-running community server.

Despite other Home mods being available, the biggest issue I usually faced with them is lack of up-to-date versions after a new Minecraft version drops. Since the Homes mod was usually the major slowdown for updating the server I decided to write my own Home mod. It also seemed like a relatively easy path to learn the basics of Fabric mod development.

For now the basic functionality is there + some moderator/operator features. I'll most probably keep it updated and keep developing it further as long as the server I manage is alive (which is probably going to be a while, at least at the current point in time).

I'm only using Fabric so I do not plan to port this mod to other platforms. The goal is to keep it manageable and update it with new Minecraft drops together with my server.

## Usage

A short-hand of available commands (aka. information posted below) is accessible when calling `/home help`.

When the mod is added to your server (or singleplayer world) it will add a new `/home` command that lets you interact with it.

Available commands:
- `/home <name>` - TPs you to a home of provided name. Omit name to TP to default home.
- `/home set <name>` - Sets a new home where player is currently located.
- `/home delete <name>` - Deletes an existing home.
- `/home default <name>` - Changes default home to <name>.
- `/home rename <home> <newname>` - renames a <home> to <newname>
- `/home list` - Lists available homes. The current default home will be printed in _italics_.

In addition to above commands there are some extra commands available only for operators:
- `/home listall` - Lists all homes known to the mod. This is useful mostly for debugging and potentially pruning the list.
- `/home deleteplayer` - Removes a player and their Homes from the collection.
- `/home import <provider>` - Imports homes from a different /home mod. See "Importing" section below for details.

## Importing

Mod has a functionality to import home data from other mods available on Modrinth. To trigger the import, as an operator call `/home import <provider>`. Available providers at this point are:
- `blossomhomes` - import from [BlossomHomes](https://modrinth.com/mod/blossomhomes) mod by CodedSakura

The import process replaces existing Home data. If the mod detects there already are some homes saved in its configuration, it will abort the import. You can confirm this is desireable and force the import by adding the keyword `overwrite` at the end of the import command - as an example, to force importing homes from BlossomHomes and overwrite existing home data call `/home import blossomhomes overwrite`.

### BlossomHomes import remarks

For import to be successful the BlossomHomes home data must be in its default location which relative to Minecraft's root directory is `saves/<world_name>/data/BlossomHomes.json`. Note that there are two `BlossomHomes.json` config files - you need the one that is saved _in the world's data directory_ and it must be present there when Import command is called. In most situations (aka. when this mod is used to replace BlossomHomes) this should already be the case and no further action is needed.

BlossomHomes only stores player IDs, while LPPP Homes stores both player IDs and usernames. For the import process online connection is recommended, as the mod will attempt to resolve the usernames based on available IDs.

BlossomHomes treats the "default home" mod-wide - there is a top-level configuration option `defaultHome` which determines which home is the default. For now this option is not supported fully and the mod might pick a random home from the collection as the default. Expect players to have to run `/home default` to reset their defaults.

BlossomHomes used to have a "legacy style" configuration which this importer does _not_ support. To import home data to LPPP Homes successfully the configuration file must come from the latest BlossomHomes version (which, at the time of writing this README, is version for Minecraft 1.21.11).

## Future plans

Below is a list of future plans I have for the mod:
- Top-level server-wide config with more administrative settings (ex. limiting amount of homes per player, cooldowns, etc.)
- Improvements/changes to command UI after we actually use this mod for a while
- Merging new home data on import
- Permission mod integration (ex. LuckPerms or similar).
- Localization support

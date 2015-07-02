# Command Item

## About

This mod adds a customizable consumable item.

## Motivation

The vanilla command block allows map makers a lot of flexibility by triggering arbitrary commands in response to redstone signals. However in some cases it
would be more natural to give the player a usable item to execute the command. Some possible use cases are:
* A teleport scroll.
* An instant buff item.
* A customized mob spawning egg.

I hoped such an item could be added to vanilla. Indeed this has been [suggested before](http://www.reddit.com/r/minecraftsuggestions/comments/16oczq/consumable_command_item/),
and also [requested as a mod](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/requests-ideas-for-mods/2381398-command-item-mod-request).

## Details

The mod adds two items:
* command\_item:command\_slate
* command\_item:command\_rune

Like the vanilla command block, they can only be obtained via /give. The command rune can be configured to take the appearance of any item, and execute commands when right clicked.

To craft a command rune, first /give a command slate:

```
/give @p command_item:command_slate
```

Craft the command slate with a book (writable or written). The book should contain the desired list of commands on the first page,
one per line. Optionally the second page may contain a customized name and description. Finally a third crafting ingredient may be added to customize the appearance.

It is also possible to produce a command rune by specifying the NBT tag directly with /give. Examples:

```
/give @p command_item:command_rune 1 0 {cmd:{cmd:["tp @p ~ ~1 ~","tell @p Up!"]}}
/give @p command_item:command_rune 1 0 {cmd:{cmd:["time set 1000"]},mimicItem:{id:"minecraft:clock"}}
```

## Future improvements

This section is essentially notes for myself...
* Fix recipe when tertiary stack has >1 item and shift click result.
* Deal with JSON formatted book contents. Can't use IChatComponent.getFormattedText on server. Can put format codes in Lore/Name NBT 
* Add other parameters (consume on use, use time, max stack?)
* Try vanilla client (acceptableRemoteVersions="")
* Right click slate to open GUI (display tab, command tab)
* Attach data from GUI to slate, to make the slate like a rune factory

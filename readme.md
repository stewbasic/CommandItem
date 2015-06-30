# Command Item

## About

This mods adds a customizable consumable item.

## Motivation

The vanilla command block allows map makers a lot of flexibility by triggering arbitrary commands in response to redstone signals. However in some cases it
would be more natural to give the player a usable item to execute the command. Some possible use cases are:
* A teleport scroll.
* An instant buff item.
* A customized mob spawning egg.
I hoped such an item could be added to vanilla. Indeed this has been [suggested before](http://www.reddit.com/r/minecraftsuggestions/comments/16oczq/consumable_command_item/),
and also [requested as a mod](http://www.minecraftforum.net/forums/mapping-and-modding/minecraft-mods/requests-ideas-for-mods/2381398-command-item-mod-request).

## Details

TODO

```
/give @p command_item:command_slate
```
* Describe crafting process
* Describe GUI

```
/give @p command_item:command_rune 1 0 {cmd:{cmd:["tp @p 0 60 0","tell @p Zap!"]}} 
```

* Vanilla client

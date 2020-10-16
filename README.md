# Nice Sleep
Spigot plugin for multiplayer servers to control how many people need to sleep for skipping the night.

## Configuration
### Percentage Needed
Percentage of not-afk players needed to skip the night (0 - 100).
0 means that any player can skip the night on their own

_Default_: 0

### Rounding Method
Rounding method for determining how many players are needed to skip the night.
The values determined from java.math.RoundingMode:
UP, DOWN, CEILING, FLOOR, HALF_UP, HALF_DOWN, HALF_EVEN. UNNECESSARY is not allowed.

_Default_: UP

### Seconds Before Skip
How long the plugin waits once a sufficient number of players is in bed before skipping the night
Note that 5.05 seconds is the vanilla waiting period

_Default_: 4.0


### OPs can overide
If set to true ops can skip the night no matter how many people are needed.

_Default_: True

### Blame
If set to true sleeping messages (<Name> went to bed. Sweet Dreams.) are shown.

_Default_: True

### Barcolor
The color of the sleeping bar.

You can choose a different color to indicate when an op sleeps, and they can skip the night in any case.

Colors are from org.bukkit.boss.BarColor:
BLUE, GREEN, PINK, PURPLE, RED, WHITE, YELLOW

_Default_ (OP): BLUE

_Default_ (Player): PINK


## A note on AFK players
The plugin does not keep track of afk players itself but relies on the command ```afkcheck``` from CMI.
If this command is not available, there won't be any distinction between afk and non-afk players.


## Changelog
# v 1.1
- Sleeping messages can now be shown
- Players in creative or spectator won't be considered for the calculation

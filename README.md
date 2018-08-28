# Tzolk'in
WIP Tzolk'in boardgame engine/ui written in Clojurescript.

```
clojure -m figwheel.main -b dev -r
```

## Goal 1
 - game includes all non-expansion game functionality
 - game history can be cycled through
 - a single public board (to be reset after each game)
 - no sign in required, any user can play any move (will need to make sure it's their turn)

## Goal 2
 - users can login / set display name
 - users can create a new board, adjust settings like max/min players
 - boards can be joined, closed, spectated
 - custom svg icons (instead of emojis)
 - actions/temples/tech can be directly clicked to make decisions
 - better round indicator (details about food days etc.)
 - wheels spin at end of turn

## Goal 3+
 - expansions
 - juicy animations

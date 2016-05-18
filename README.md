# Tzolk'in
WIP Tzolk'in boardgame in clojurescript using vanilla reagent (just passing full state around).

If you want to have a poke around run `lein figwheel`, open
`localhost:3449/cards.html` in your browser and behold the magic of
[devcards](https://github.com/bhauman/devcards).

## Version 1
 - game includes all non-expansion game functionality
 - infinite undo
 - a single permanent board (to be reset after each game)
 - no sign in required, any user can play any move (will need to make sure it's their turn)

## Version 2
 - users sign up and login via firebase
 - users set their display name
 - users can create a new board, adjust settings like max/min players
 - boards can be joined, closed, spectated
 - custom svg icons (instead of emojis)
 - actions/temples/tech can be directly clicked to make decisions
 - better round indicator (details about food days etc.)
 - wheels spin at end of turn
 - game history can be cycled through

## Version 3+
 - expansions
 - juicy animations

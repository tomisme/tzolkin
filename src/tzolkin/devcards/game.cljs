(ns tzolkin.devcards.game
  (:require-macros
   [devcards.core :as dc :refer [defcard defcard-rg defcard-doc deftest]]))

(defcard-doc
  "
  #PLAYER TURNS

  Begin with the starting player and proceed clockwise. Your turn proceeds in the following order:

    a. Beg for corn: If you have 2 or fewer corn, you may replenish to 3. This angers the gods.
    b. You must choose one of these two options:
      I. Place any number of workers. A worker is always placed on the lowest-numbered unoccupied action space on the gear. The amount paid for worker placement depends on:
        1. the number of workers placed, according to the table on the player boards;
        2. the action spaces where the workers are placed.
      II. or Pick up any number of workers, one by one. For each worker picked up, do one of the following:
        1. Perform the action depicted at the worker's action space.
        2. Or perform a lower-numbered action on the same gear, paying 1 corn for each step back.
        3. Or perform no action.
    c. If you constructed any buildings, deal out new buildings from the current age into the emptied spaces.
    d. If you placed a worker on the Starting Player Space, take all the corn that has accumulated on the teeth of the Tzolk'in gear.

  #FEED WORKERS AND GET REWARDS.

  This phase of the round only occurs on Food Days.

  a. You must pay 2 corn for each worker you have
  in play. You lose 3 victory points for each worker
  you cannot feed.
  b. If the Food Day is the end of Age 1, all buildings
  are discarded from the game board and Age 2
  buildings are dealt.
  c. At each temple, players get rewards from the
  temple's god. Each temple is resolved separately.
   I. In the middle of an age (brown-orange Food)
  Days players get the resources or crystal
  skulls depicted on the left side of the steps.
  You get the reward for your current step and
  all steps below.
   II. At the end of an age (blue-green Food Days)
  players get victory points.
   1. You score the victory points depicted on
  the right side of your current step (but)
  not the steps below.
   2. The highest-ranked player gets a bonus,
  as indicated above the temple. The lower
  left number is awarded at the end of Age 1.
  The upper right number is awarded at the
  end of Age 2. If players are tied for highest,
  all tied players get half the bonus.

  #ADVANCE THE TZOLK'IN CALENDAR:
  
  a. If no worker is on the Starting Player Space, do
  the following:
   I. Put 1 corn on the current tooth of the
  Tzolk'in gear.
   II. Advance the Tzolk'in gear 1 day. Any workers
  pushed off the gears return to their players.
  b. If your worker is on the Starting Player Space, do
  the following:
   I. Take back your worker from the Tzolk'in gear.
   II. Either take the Starting Player Marker from
  another player; or, if you already have the
  Starting Player Marker, pass it to the left.
   III. Advance the calendar 1 day. If your player
  board is lighter-side-up, you may advance the
  calendar 2 days instead, as long as the extra
  day does not push any worker off the gears.
  Flip your player board to the darker side if you
  use this privilege. Any workers pushed off the
  gears return to their players.
  ")

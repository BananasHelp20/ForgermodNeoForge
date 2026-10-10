# Marked ability implementation record

Only entries marked for implementation in abilityRecommendations.md are approved. Repeated entries share one implementation across their listed weapon families and gemstone variants. Abilities are learned through augmentation; each weapon retains two active and two unique passive slots, plus Material Hit separately. Missing balance values below were chosen with the user's permission.

## Quench Point — completed

- Weapons: Emberfang Dagger, including Ruby, Amber, Amethyst and Jade.
- Successful melee hits extinguish a burning victim. Up to ten seconds of remaining fire become a stored bonus for the next successful hit within ten seconds.
- Ranks I–IV convert each second into 0.5, 0.75, 1 and 1.25 damage respectively (caps 5, 7.5, 10 and 12.5).
- A hit can spend a previous charge and store fire from its new victim; unsuccessful or blocked hits do not spend it. Switching, death, logout and expiry clear stored fire.
- Reviewed twice. Build/lightweight checks and 60 required server tests pass, including all five variants, one-time use, rejected hits, switch cleanup, cap and expiry.

## Remaining marked abilities — pending

Explosion; Cinder Decoy; Heating Up; Cold Bellows; Fire Resistance; Explosive Hits (active); Nether Born; Crucible Hook; Strong-arm; Swing Attack; Death Stare; Echoing Speed; Distance Tax; Null Receipt; Overwhelming Smash; False Awakening; Delusion; Lonely Dream; Unfinished Dream; Just a Dream; Sleepwalker; Static Blade; Lightning Rod; Alternating Current; Direct Current / Static Field; Stunning Presence; Downdraft Bell; Crosswind; Slipstream; Upwind; Windcharge / Skybreaker; Pocket Storm; Final Strike; Blood Sacrifice; Scar Compass; Revengeful Exit.

Heating Up's unfinished maximum-level fragment does not yet define when to extinguish fire. Its explicitly described heat storage and ignition behavior remain approved.

# Project instructions

- Always target Minecraft **1.21.1** and NeoForge **21.1.x**. The current `gradle.properties` specifies NeoForge **21.1.93**. Check APIs against this version.
- Record every addition, deletion, buff, and debuff as a list entry in `src/changes.txt`.
- Implement weapon abilities one at a time. After each ability, test it, review the code for bugs, fix them, rerun the tests, review again, then commit and push before proceeding to the next ability. If Git commit or push fails, report the commands the user can run and continue as instructed.
- Treat `ability-thoughts.txt` as user-authored design input. Do not change it unless asked. The content after `[CODEX IGNOGE THE FOLLOWING TEXT]` is excluded from the currently approved ability list.
- Primary and secondary ability keys are configurable and default to `1` and `2`. Show the configured key in the item tooltip; do not hardcode the displayed key.
- Primary and secondary cooldowns are independent for each registered weapon variant. They can be active at the same time on the same equipped stack. Switching away cancels ongoing ability state and starts the relevant cooldown. Instant abilities start their cooldown on use.
- Daggers can dual wield with another dagger of the same material in the offhand, including different gemstone variants. Knives are excluded. Axes use the third-attack area slam and disable the offhand.
- Keep `sessionContext.md` current when handing off substantial work.

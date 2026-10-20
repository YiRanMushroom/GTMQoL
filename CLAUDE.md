# GTMQoL

Start of a session:
1. Read `context.md` (branches, known unfixed bugs, to-dos, open questions). Update it when a piece of work is done.
2. `index_open("<repo root>")` (agent-toolkit doc index, `.agent-index.db`; never read that file directly), then
   `index_read` the `rules.*` entries (`index_list("rules")`): general and project rules, mixin conventions, source
   layout. Build setup: `build.*`. Areas: `index_keys("gtmqol")`. Before working on a feature, `index_find` it.

Always:
- Reply in Chinese.
- Never run the game (`runClient`, `runServer`); build / `compileJava` / `runData` are fine.
- Everything else (commits, connectors, abstractions, hacky code, docs) is in `rules.general` / `rules.working`.

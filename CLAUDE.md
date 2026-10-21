# GTMQoL

Start of a session:
1. Read `context.md` (branches, known unfixed bugs, to-dos, open questions). Update it when a piece of work is done.
2. agent-toolkit doc index: the server opens it at start (saved as text in `.agent-toolkit/index/`, commit changes
   with `index_save`; the cache `.agent-toolkit/cache/` is local). `index_read` the `rules.*` entries
   (`index_list("rules")`): project rules, mixin conventions, source layout. Build setup: `build.*`. Areas:
   `index_keys("gtmqol")`. Before working on a feature, `index_find` it. With both worktrees open, pass `repo`.

Always:
- Reply in Chinese.
- Never run the game (`runClient`, `runServer`); build / `compileJava` / `runData` are fine.
- General rules (commits, connectors, abstractions, hacky code, docs) come with agent-toolkit's instructions; this
  project's own are in `rules.working`.

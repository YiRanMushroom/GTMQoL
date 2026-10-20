# GTMQoL (1.20.1)

Start of a session:
1. Read `context.md` (this branch's open items) and 1.21.1's `context.md` (shared to-dos, open questions): the
   1.21.1 worktree is next to this one (`..\GTMQoL`), or `git show 1.21:context.md`. Update this branch's when
   a piece of work is done.
2. `index_open("<repo root>")` (agent-toolkit doc index, `.agent-index.db`; never read that file directly), then
   `index_read` the `rules.*` entries (`index_list("rules")`): general and project rules, mixin conventions, source
   layout. Build setup: `build.*`. Areas: `index_keys("gtmqol")`. Before working on a feature, `index_find` it.
   This branch's index has the same ids as 1.21.1's, linked to this branch's code.

Always:
- Reply in Chinese.
- Never run the game (`runClient`, `runServer`); build / `compileJava` / `runData` are fine.
- Everything else (commits, connectors, abstractions, hacky code, docs) is in `rules.general` / `rules.working`.

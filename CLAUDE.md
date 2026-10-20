# GTMQoL (1.20.1)

Start of a session:
1. Read `context.md` (this branch's open items) and 1.21.1's `context.md` (shared to-dos, open questions): the
   1.21.1 worktree is next to this one (`..\GTMQoL`), or `git show port/1.21.1:context.md`. Update this branch's when
   a piece of work is done; it holds only general notes and current work, no "not tested yet" status and no design
   details.
2. `index_open("<repo root>")` (agent-toolkit doc index, `.agent-index.db`; never read that file directly), then
   `index_read` the `rules.*` entries (`index_list("rules")`): working rules and mixin conventions. Build setup:
   `build.*`. Before working on a feature, `index_find` it. This branch's index has the same ids as 1.21.1's, linked
   to this branch's code.

Always:
- Reply in Chinese.
- Commits and PRs get no `Co-Authored-By` and no Claude attribution unless the user asks for it.
- Never mention claude.ai connectors (Atlassian, Microsoft 365, Gmail, Google Calendar, any MCP connector) or their
  auth status. The user doesn't use them.
- Never run the game (`runClient`, `runServer`); build / `compileJava` / `runData` are fine.
- Abstractions are proposed and wait for the user; hacky designs are reported before use (`rules.working`).
- Non-obvious knowledge goes into a source comment, then an index entry. `docs/*.md` are retired.

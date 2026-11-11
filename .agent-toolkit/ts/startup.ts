// ts_exec helpers for changing both worktrees at once (this one is 1.21; 1.20 sits beside it as GTMQoL-1.20.1).
// Loaded into every ts_exec session. Paths in `rel` are under src/main/java/com/yiran/minecraft/gtmqol/, or start with
// "/" for a path from the worktree root ("/README.md").

const ROOTS: Record<string, string> = { "21": cwd(), "20": `${cwd()}/../GTMQoL-1.20.1` };
const SRC = "/src/main/java/com/yiran/minecraft/gtmqol/";

function pathIn(branch: string, rel: string): string {
  return rel.startsWith("/") ? ROOTS[branch] + rel : ROOTS[branch] + SRC + rel;
}

function eachBranch(only: string | undefined, rel: string, change: (text: string, branch: string) => string): void {
  for (const branch of Object.keys(ROOTS)) {
    if (only && only !== branch) continue;
    const file = pathIn(branch, rel);
    if (!exists(file)) {
      print(`[${branch}] no such file: ${rel}`);
      continue;
    }
    const raw = read(file);
    const crlf = raw.includes("\r\n");
    const text = change(raw.replace(/\r\n/g, "\n"), branch);
    if (text !== raw.replace(/\r\n/g, "\n")) write(file, crlf ? text.replace(/\n/g, "\r\n") : text);
  }
}

/**
 * The same exact-text edits in both worktrees: both("common/x/Y.java", [[old, new], ...], only?). An old text that
 * isn't there exactly once is reported for that branch and skipped (the branches differ in places), the rest applies.
 */
function both(rel: string, pairs: [string, string][], only?: string): void {
  eachBranch(only, rel, (text, branch) => {
    pairs.forEach(([from, to], i) => {
      const at = text.indexOf(from);
      if (at < 0) return print(`[${branch}] not found ${rel} #${i + 1}: ${from.slice(0, 70).replace(/\n/g, "\\n")}`);
      if (text.indexOf(from, at + 1) >= 0) return print(`[${branch}] more than once ${rel} #${i + 1}`);
      text = text.slice(0, at) + to + text.slice(at + from.length);
    });
    return text;
  });
}

/**
 * Replaces one lang text (a Java string, also when it is split over lines with `" +`) in both worktrees:
 * lang("common/x/Y.java", "Old text", "New text"). Texts as written in the source (escapes included).
 */
function lang(rel: string, oldText: string, newText: string, only?: string): void {
  eachBranch(only, rel, (text, branch) => {
    const literal = /"((?:\\.|[^"\\\n])*)"(?:\s*\+\s*"((?:\\.|[^"\\\n])*)")*/g;
    const hits: { at: number; length: number }[] = [];
    for (let m; (m = literal.exec(text)); ) {
      const joined = [...m[0].matchAll(/"((?:\\.|[^"\\\n])*)"/g)].map((x) => x[1]).join("");
      if (joined === oldText) hits.push({ at: m.index, length: m[0].length });
    }
    if (hits.length !== 1) {
      print(`[${branch}] ${hits.length} matches in ${rel}: ${oldText.slice(0, 60)}`);
      return text;
    }
    const { at, length } = hits[0]!;
    const lineStart = text.lastIndexOf("\n", at) + 1;
    const indent = text.slice(lineStart).match(/^\s*/)![0] + "        ";
    // split on spaces so no line goes past ~118 columns
    const parts: string[] = [];
    let rest = newText;
    let room = Math.max(40, 116 - (at - lineStart));
    while (rest.length > room) {
      let cut = rest.lastIndexOf(" ", room);
      if (cut <= 0) cut = room;
      parts.push(rest.slice(0, cut + 1));
      rest = rest.slice(cut + 1);
      room = 116 - indent.length;
    }
    parts.push(rest);
    return text.slice(0, at) + parts.map((x) => `"${x}"`).join(` +\n${indent}`) + text.slice(at + length);
  });
}

/** Prints lines from..to of a source file with line numbers: show("common/x/Y.java", 10, 40, "20"). */
function show(rel: string, from: number, to: number, branch = "21"): void {
  const lines = read(pathIn(branch, rel)).split(/\r?\n/);
  print(`--- [${branch}] ${rel}:${from}`);
  print(lines.slice(from - 1, to).map((l, i) => `${from + i}\t${l}`).join("\n"));
}

/** Adds an import line after the last non-java import, unless it is there: addImport("21", rel, "import a.b.C;"). */
function addImport(branch: string, rel: string, line: string): void {
  eachBranch(branch, rel, (text) => {
    if (text.includes(line)) return text;
    const lines = text.split("\n");
    let last = -1;
    lines.forEach((l, i) => {
      if (/^import (?!static |java\.|javax\.)/.test(l)) last = i;
    });
    lines.splice(last + 1, 0, line);
    return lines.join("\n");
  });
}

/** Removes imports nothing in the file uses (javadoc {@link} counts as a use, strings and // comments don't). */
function cleanImports(): void {
  for (const branch of Object.keys(ROOTS)) {
    for (const f of glob("src/main/java/**/*.java", { cwd: ROOTS[branch] })) {
      const file = `${ROOTS[branch]}/${f.replace(/\\/g, "/")}`;
      let text = read(file);
      let body = text.replace(/^import .*;$/gm, "");
      const linked: string[] = [];
      body = body.replace(/\/\*[\s\S]*?\*\//g, (c) => {
        for (const m of c.matchAll(/\{@link(?:plain)?\s+([^}]*)\}|@see\s+(\S+)|@throws\s+(\S+)/g)) linked.push(m[1] ?? m[2] ?? m[3]!);
        return "";
      });
      body = `${body.replace(/"(?:\\.|[^"\\\n])*"/g, '""').replace(/\/\/.*$/gm, "")}\n${linked.join("\n")}`;
      for (const m of text.matchAll(/^import (?:static )?[\w.]+\.(\w+);\r?\n/gm)) {
        if (new RegExp(`\\b${m[1]}\\b`).test(body)) continue;
        text = text.replace(m[0], "");
        print(`[${branch}] ${f.replace(/\\/g, "/").split("gtmqol/").pop()}: ${m[0].trim()}`);
      }
      if (text !== read(file)) write(file, text);
    }
  }
}

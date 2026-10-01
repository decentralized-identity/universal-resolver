const ANSI_ESCAPE = /\u001b\[[0-9;]*[A-Za-z]/g;
/** Stack frames and nested causes, e.g. `    at x.y(Y.java:1)`, `    ... 31 more`, `Caused by: …`. */
const STACK_TRACE = /^(\s+at\s|\s*\.\.\. \d+ more|\s*Caused by:)/;
/** Log lines with a problem level: uppercase levels (`WARN`, `ERROR`, …) or structured logs (`level=error`, `"level":"error"`). */
const PROBLEM_LEVEL = /\b(WARN|WARNING|ERROR|SEVERE|FATAL|CRITICAL|PANIC)\b/;
const STRUCTURED_PROBLEM_LEVEL = /\blevel=(warn|warning|error|fatal)\b|"level":\s*"(warn|warning|error|fatal)"/i;
/** The message line of an exception, e.g. `uniresolver.ResolutionException: …`. */
const EXCEPTION = /^[\w.$]+(Exception|Error)\b/;

/**
 * Reduces the log lines of one request to the ones that tell what went wrong: lines with a warning or error
 * level and exception messages, without stack traces. Falls back to the last lines of the request if no line
 * has a problem level. The goal is to point the contributor to the problem, not to fully explain it.
 */
export function relevantLines(log: string, maxLines: number): string[] {
  const lines = log
    .replace(ANSI_ESCAPE, '')
    .split(/\r?\n/)
    .map((line) => line.trimEnd())
    .filter((line) => line.trim() && !STACK_TRACE.test(line));

  const problems = lines.filter((line) => PROBLEM_LEVEL.test(line) || STRUCTURED_PROBLEM_LEVEL.test(line) || EXCEPTION.test(line));
  return problems.length ? problems.slice(0, maxLines) : lines.slice(-maxLines);
}

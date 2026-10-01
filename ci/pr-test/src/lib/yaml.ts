import { parseDocument } from 'yaml';

/** Parses YAML, throwing a readable error with the position of the first problem. */
export function parseYaml(text: string, name: string): unknown {
  const document = parseDocument(text, { uniqueKeys: true, merge: true });
  const [error] = document.errors;
  if (error) {
    const position = error.linePos?.[0];
    const location = position ? ` at line ${position.line}, column ${position.col}` : '';
    throw new Error(`${name} is not valid YAML${location}: ${error.message.split('\n')[0]}`);
  }
  return document.toJS();
}

export const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null && !Array.isArray(value);

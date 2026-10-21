import { parseDocument } from 'yaml';

export type YamlResult = { ok: true; value: unknown } | { ok: false; error: string };

/**
 * Parses YAML strictly: duplicate keys and tabs used for indentation are errors, as they are for
 * Spring (application.yml) and docker compose.
 */
export function parseYaml(text: string): YamlResult {
  const document = parseDocument(text, { uniqueKeys: true, merge: true });
  const [error] = document.errors;
  if (error) {
    const position = error.linePos?.[0];
    const location = position ? `line ${position.line}, column ${position.col}: ` : '';
    const message = error.message.split('\n')[0]?.replace(/ at line \d+, column \d+:?$/, '');
    return { ok: false, error: `Invalid YAML at ${location}${message}` };
  }
  return { ok: true, value: document.toJS() };
}

export const isRecord = (value: unknown): value is Record<string, unknown> =>
  typeof value === 'object' && value !== null && !Array.isArray(value);

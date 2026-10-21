import { randomUUID } from 'node:crypto';
import { appendFile } from 'node:fs/promises';

/** Writes step outputs and the job summary when running in GitHub Actions; no-op otherwise. */
export async function writeActionOutputs(outputs: Record<string, string | number>, summary: string): Promise<void> {
  const outputFile = process.env.GITHUB_OUTPUT;
  if (outputFile) {
    const lines = Object.entries(outputs).map(([name, value]) => {
      const text = String(value);
      if (!text.includes('\n')) return `${name}=${text}\n`;
      const delimiter = `EOF_${randomUUID()}`;
      return `${name}<<${delimiter}\n${text}\n${delimiter}\n`;
    });
    await appendFile(outputFile, lines.join(''));
  }
  const summaryFile = process.env.GITHUB_STEP_SUMMARY;
  if (summaryFile) await appendFile(summaryFile, summary);
}

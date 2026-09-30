import { CHECKED_FILES } from '../config.ts';
import type { Section } from '../report/report.ts';

const allowed: ReadonlySet<string> = new Set(CHECKED_FILES);

/** A driver pull request may only change the checked files; the driver code belongs in its own repository. */
export function checkChangedFiles(section: Section, changedFiles: string[]): void {
  for (const name of [...new Set(changedFiles)].sort()) {
    if (!allowed.has(name)) {
      section.error(
        `\`${name}\` must not be changed, only the 4 files above may be edited ` +
          '(the driver code belongs in its own repository and is referenced by its image)',
      );
    }
  }
}

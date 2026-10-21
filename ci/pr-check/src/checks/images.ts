import { mapConcurrent } from '../lib/concurrency.ts';
import { checkPullable, type Pullability } from '../lib/registry.ts';
import type { Section } from '../report/report.ts';

const CONCURRENT_REQUESTS = 8;

export const NOT_PULLABLE_NOTE =
  'If this is not your image, that is not pullable, the error can be treated as a warning. ' +
  'Be aware that docker compose might not work with that particular image in the services.';

/** Checks that every image can be pulled without a login. images: image reference → service names. */
export async function checkImages(
  section: Section,
  images: Map<string, string[]>,
  resolve: (image: string) => Promise<Pullability> = checkPullable,
): Promise<void> {
  const references = [...images.keys()].sort();
  const results = await mapConcurrent(references, CONCURRENT_REQUESTS, resolve);

  references.forEach((image, index) => {
    const result = results[index];
    if (!result || result.pullable) return;
    const services = (images.get(image) ?? []).map((s) => `\`${s}\``).join(', ');
    const message = `Image \`${image}\` (service ${services}) ${result.reason}`;
    if (result.level === 'error') {
      section.error(message);
      section.info(NOT_PULLABLE_NOTE);
    } else {
      section.warning(message);
    }
  });
}

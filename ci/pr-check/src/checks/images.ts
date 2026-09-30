import { mapConcurrent } from '../lib/concurrency.ts';
import { checkPullable, type Pullability } from '../lib/registry.ts';
import type { Section } from '../report/report.ts';

const CONCURRENT_REQUESTS = 8;

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
    if (result.level === 'error') section.error(message);
    else section.warning(message);
  });
}

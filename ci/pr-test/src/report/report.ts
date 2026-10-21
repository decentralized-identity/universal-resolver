export interface ServiceStartup {
  service: string;
  started: boolean;
  /** Container status, e.g. `running` or `exited (1)`. */
  status: string;
  /** Complete log of the container up to the check. */
  log: string;
}

export interface LogExcerpt {
  service: string;
  lines: string[];
}

export interface IdentifierResult {
  did: string;
  /** Pattern of the driver the identifier belongs to. */
  pattern: string;
  ok: boolean;
  /** Why resolving failed. */
  reason?: string;
  durationMs: number;
  /** Relevant log lines of the driver and the resolver for a failed identifier. */
  logs: LogExcerpt[];
}

/** A problem that prevents (part of) the test, e.g. an image that can't be pulled. */
export interface SetupProblem {
  message: string;
  logs?: LogExcerpt[];
}

export class TestReport {
  subject: string | undefined;
  /** Drivers selected for the test, with the reason, e.g. "driver entry changed". */
  readonly tested: { pattern: string; reason: string }[] = [];
  readonly problems: SetupProblem[] = [];
  readonly startups: ServiceStartup[] = [];
  readonly results: IdentifierResult[] = [];

  get failedCount(): number {
    return this.results.filter((r) => !r.ok).length;
  }

  get passed(): boolean {
    return this.problems.length === 0 && this.startups.every((s) => s.started) && this.failedCount === 0;
  }

  /** Nothing in the pull request needs a test run, e.g. only the README changed. */
  get nothingToTest(): boolean {
    return this.tested.length === 0 && this.problems.length === 0;
  }
}

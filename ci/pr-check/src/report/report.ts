export type Level = 'error' | 'warning';

export interface Finding {
  level: Level;
  message: string;
}

/** Findings of one part of the check, e.g. one file. */
export class Section {
  readonly findings: Finding[] = [];
  readonly name: string;
  /** Render the name as code, e.g. a file path. */
  readonly code: boolean;

  constructor(name: string, code: boolean) {
    this.name = name;
    this.code = code;
  }

  error(message: string): void {
    this.findings.push({ level: 'error', message });
  }

  warning(message: string): void {
    this.findings.push({ level: 'warning', message });
  }

  get errors(): Finding[] {
    return this.findings.filter((f) => f.level === 'error');
  }

  get warnings(): Finding[] {
    return this.findings.filter((f) => f.level === 'warning');
  }
}

export class Report {
  readonly sections: Section[] = [];
  /** Optional line describing what was checked, e.g. the pull request. */
  subject: string | undefined;

  /** Returns the section with this name, creating it (at the end) if needed. */
  section(name: string, { code = true }: { code?: boolean } = {}): Section {
    let section = this.sections.find((s) => s.name === name);
    if (!section) {
      section = new Section(name, code);
      this.sections.push(section);
    }
    return section;
  }

  get errorCount(): number {
    return this.sections.reduce((sum, s) => sum + s.errors.length, 0);
  }

  get warningCount(): number {
    return this.sections.reduce((sum, s) => sum + s.warnings.length, 0);
  }

  get passed(): boolean {
    return this.errorCount === 0;
  }
}

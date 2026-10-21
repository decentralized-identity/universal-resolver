import { execFile } from 'node:child_process';

export interface ExecResult {
  code: number;
  stdout: string;
  stderr: string;
}

/** Runs a command and resolves with its exit code and output; never rejects for a non-zero exit code. */
export function exec(command: string, args: string[], options: { timeoutMs?: number } = {}): Promise<ExecResult> {
  return new Promise((resolve, reject) => {
    execFile(
      command,
      args,
      { timeout: options.timeoutMs ?? 0, maxBuffer: 256 * 1024 * 1024 },
      (error, stdout, stderr) => {
        if (error && (error as NodeJS.ErrnoException).code === 'ENOENT') {
          reject(new Error(`\`${command}\` not found, is it installed?`));
          return;
        }
        const code = error ? (typeof error.code === 'number' ? error.code : 1) : 0;
        resolve({ code, stdout, stderr });
      },
    );
  });
}

/** Like exec, but rejects with the command's error output if it fails. */
export async function execOrThrow(command: string, args: string[], options: { timeoutMs?: number } = {}): Promise<string> {
  const result = await exec(command, args, options);
  if (result.code !== 0) {
    const output = (result.stderr || result.stdout).trim();
    throw new Error(`\`${command} ${args.slice(0, 6).join(' ')} …\` failed: ${output || `exit code ${result.code}`}`);
  }
  return result.stdout;
}

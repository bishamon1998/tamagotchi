const REQUEST_TIMEOUT_MS = 10_000;

export type Me = { id: number; firstName: string; language: string };

/** Thrown when the backend refuses the Telegram sign-in, as opposed to being unreachable. */
export class SignInError extends Error {}

/** Loads the signed-in user. The backend checks the Telegram signature in the Authorization header. */
export async function fetchMe(initData: string): Promise<Me> {
  const response = await fetch("/api/v1/me", {
    headers: { Authorization: `tma ${initData}` },
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
  });
  if (response.status === 401) throw new SignInError();
  if (!response.ok) throw new Error(`Backend answered ${response.status}`);
  return response.json();
}

import type { PetAction, PetNeeds } from "./pet/petNeeds";

const REQUEST_TIMEOUT_MS = 10_000;

export type Me = { id: number; firstName: string; language: string };

/** Thrown when the backend refuses the Telegram sign-in, as opposed to being unreachable. */
export class SignInError extends Error {}

/** Loads the signed-in user. */
export function fetchMe(initData: string): Promise<Me> {
  return request("/api/v1/me", initData);
}

/** Loads the user's pet; the first visit hatches it. */
export function fetchPet(initData: string): Promise<PetNeeds> {
  return request("/api/v1/pet", initData);
}

/** Applies a care action and returns the pet's needs after it. */
export function careForPet(initData: string, action: PetAction): Promise<PetNeeds> {
  return request(`/api/v1/pet/actions/${action}`, initData, "POST");
}

/** Calls the API as the signed-in user. The backend checks the Telegram signature in the Authorization header. */
async function request<T>(path: string, initData: string, method = "GET"): Promise<T> {
  const response = await fetch(path, {
    method,
    headers: { Authorization: `tma ${initData}` },
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
  });
  if (response.status === 401) throw new SignInError();
  if (!response.ok) throw new Error(`Backend answered ${response.status}`);
  return response.json();
}

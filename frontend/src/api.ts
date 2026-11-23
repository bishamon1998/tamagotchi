import type { CareResult, PetAction, PetNeeds } from "./pet/petNeeds";
import type { PetHome } from "./pet/petHome";
import type { Wallet } from "./pet/wallet";
import type { DecorItemId, DecorShop } from "./decor/decor";

const REQUEST_TIMEOUT_MS = 10_000;

type Method = "GET" | "POST" | "PUT" | "DELETE";

export type Me = { id: number; firstName: string; language: string };

/** Thrown when the backend refuses the Telegram sign-in, as opposed to being unreachable. */
export class SignInError extends Error {}

/** Loads the signed-in user. */
export function fetchMe(initData: string): Promise<Me> {
  return request("/api/v1/me", initData);
}

/** Loads the pet, the coins and the decor shop. The first visit hatches the pet; the day's first visit pays the bonus. */
export async function fetchPetHome(initData: string): Promise<PetHome> {
  const [needs, wallet, decor] = await Promise.all([
    request<PetNeeds>("/api/v1/pet", initData),
    request<Wallet>("/api/v1/wallet", initData),
    request<DecorShop>("/api/v1/decor", initData),
  ]);
  return { needs, wallet, decor };
}

/** Applies a care action and returns the pet and the balance after it. A treat the user cannot afford fails. */
export function careForPet(initData: string, action: PetAction): Promise<CareResult> {
  return request(`/api/v1/pet/actions/${action}`, initData, "POST");
}

/** Buys a decor item; it goes straight into the room. */
export function buyDecor(initData: string, item: DecorItemId): Promise<DecorShop> {
  return request(`/api/v1/decor/${item}/purchase`, initData, "POST");
}

/** Puts an owned item in the room in place of the one in its slot. */
export function placeDecor(initData: string, item: DecorItemId): Promise<DecorShop> {
  return request(`/api/v1/decor/${item}/placement`, initData, "PUT");
}

/** Takes an item out of the room; it stays owned. */
export function removeDecor(initData: string, item: DecorItemId): Promise<DecorShop> {
  return request(`/api/v1/decor/${item}/placement`, initData, "DELETE");
}

/** Calls the API as the signed-in user. The backend checks the Telegram signature in the Authorization header. */
async function request<T>(path: string, initData: string, method: Method = "GET"): Promise<T> {
  const response = await fetch(path, {
    method,
    headers: { Authorization: `tma ${initData}` },
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
  });
  if (response.status === 401) throw new SignInError();
  if (!response.ok) throw new Error(`Backend answered ${response.status}`);
  return response.json();
}

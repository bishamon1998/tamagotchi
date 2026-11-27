import type { CareResult, PetAction, PetState } from "./pet/petNeeds";
import type { PetHome } from "./pet/petHome";
import type { Wallet } from "./pet/wallet";
import type { DecorItemId, DecorShop } from "./decor/decor";

const REQUEST_TIMEOUT_MS = 10_000;

type Method = "GET" | "POST" | "PUT" | "DELETE";

/** The signed-in user; testers also get the tester tools. */
export type Me = { id: number; firstName: string; language: string; tester: boolean };

/** Thrown when the backend refuses the Telegram sign-in, as opposed to being unreachable. */
export class SignInError extends Error {}

/** Loads the signed-in user. */
export function fetchMe(initData: string): Promise<Me> {
  return request("/api/v1/me", initData);
}

/** Loads the pet, the coins and the decor shop. The first visit hatches the pet; the day's first visit pays the bonus. */
export async function fetchPetHome(initData: string): Promise<PetHome> {
  const [pet, wallet, decor] = await Promise.all([
    fetchPet(initData),
    request<Wallet>("/api/v1/wallet", initData),
    request<DecorShop>("/api/v1/decor", initData),
  ]);
  return { pet, wallet, decor };
}

/** Loads the pet as it is now, for example once it has woken up. */
export function fetchPet(initData: string): Promise<PetState> {
  return request("/api/v1/pet", initData);
}

/** Applies a care action and returns the pet and the balance after it. A sleeping pet or an unaffordable treat fails. */
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

/** Tester tools. They change only the tester's own game, so the app reloads the pet afterwards. */
export type TesterTool = "coins" | "skip-hours" | "skip-day" | "start-over";

const TESTER_REQUESTS: Record<TesterTool, { path: string; method: Method }> = {
  coins: { path: "/api/v1/tester/coins", method: "POST" },
  "skip-hours": { path: "/api/v1/tester/skip-hours", method: "POST" },
  "skip-day": { path: "/api/v1/tester/skip-day", method: "POST" },
  "start-over": { path: "/api/v1/tester/progress", method: "DELETE" },
};

export async function runTesterTool(initData: string, tool: TesterTool): Promise<void> {
  const { path, method } = TESTER_REQUESTS[tool];
  await send(path, initData, method);
}

/** Calls the API and reads its JSON answer. */
async function request<T>(path: string, initData: string, method: Method = "GET"): Promise<T> {
  const response = await send(path, initData, method);
  return response.json();
}

/** Calls the API as the signed-in user. The backend checks the Telegram signature in the Authorization header. */
async function send(path: string, initData: string, method: Method): Promise<Response> {
  const response = await fetch(path, {
    method,
    headers: { Authorization: `tma ${initData}` },
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
  });
  if (response.status === 401) throw new SignInError();
  if (!response.ok) throw new Error(`Backend answered ${response.status}`);
  return response;
}

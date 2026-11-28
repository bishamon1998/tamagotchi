import type { CareResult, PetAction, PetState } from "./pet/petNeeds";
import type { PetHome } from "./pet/petHome";
import type { Wallet } from "./pet/wallet";
import type { DecorItemId, DecorShop } from "./decor/decor";
import type { FlowerId, Garden, SeedKind } from "./garden/garden";

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

/**
 * Loads the pet, the coins, the decor shop and the vase. The first visit hatches the pet; the day's first visit pays
 * the bonus and brings the free seed.
 */
export async function fetchPetHome(initData: string): Promise<PetHome> {
  const [pet, wallet, decor, garden] = await Promise.all([
    fetchPet(initData),
    request<Wallet>("/api/v1/wallet", initData),
    request<DecorShop>("/api/v1/decor", initData),
    fetchGarden(initData),
  ]);
  return { pet, wallet, decor, vase: garden.vase };
}

/** Loads the pet as it is now, for example once it has woken up. */
export function fetchPet(initData: string): Promise<PetState> {
  return request("/api/v1/pet", initData);
}

/** Names the pet, or renames it; the backend trims the name and refuses a blank or too long one. */
export function namePet(initData: string, name: string): Promise<PetState> {
  return request("/api/v1/pet/name", initData, "PUT", { name });
}

/** Strokes the pet; it cheers up a little at most every half a minute (the backend decides). */
export function strokePet(initData: string): Promise<PetState> {
  return request("/api/v1/pet/stroke", initData, "POST");
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

/** Loads the garden; the day's first visit also brings the free seed. */
export function fetchGarden(initData: string): Promise<Garden> {
  return request("/api/v1/garden", initData);
}

export function buySeed(initData: string, kind: SeedKind): Promise<Garden> {
  return request(`/api/v1/garden/seeds/${kind}/purchase`, initData, "POST");
}

/** Plants a seed in an empty bed; what it grows into shows only when it blooms. */
export function plantSeed(initData: string, bed: number, kind: SeedKind): Promise<Garden> {
  return request(`/api/v1/garden/beds/${bed}/plant/${kind}`, initData, "POST");
}

/** Waters a growing plant, once a day. */
export function waterBed(initData: string, bed: number): Promise<Garden> {
  return request(`/api/v1/garden/beds/${bed}/water`, initData, "POST");
}

/** Picks a bloomed flower into the basket and frees the bed. */
export function pickFlower(initData: string, bed: number): Promise<Garden> {
  return request(`/api/v1/garden/beds/${bed}/pick`, initData, "POST");
}

/** Sells one picked flower at the market. */
export function sellFlower(initData: string, flower: FlowerId): Promise<Garden> {
  return request(`/api/v1/garden/basket/${flower}/sale`, initData, "POST");
}

/** Puts one picked flower in the vase at home. */
export function putInVase(initData: string, flower: FlowerId): Promise<Garden> {
  return request(`/api/v1/garden/vase/${flower}`, initData, "PUT");
}

/** Tester tools. They change only the tester's own game, so the app reloads the pet afterwards. */
export type TesterTool = "coins" | "skip-hours" | "skip-day" | "start-over" | "seeds" | "bloom" | "flowers";

const TESTER_REQUESTS: Record<TesterTool, { path: string; method: Method }> = {
  coins: { path: "/api/v1/tester/coins", method: "POST" },
  "skip-hours": { path: "/api/v1/tester/skip-hours", method: "POST" },
  "skip-day": { path: "/api/v1/tester/skip-day", method: "POST" },
  "start-over": { path: "/api/v1/tester/progress", method: "DELETE" },
  seeds: { path: "/api/v1/tester/seeds", method: "POST" },
  bloom: { path: "/api/v1/tester/bloom", method: "POST" },
  flowers: { path: "/api/v1/tester/flowers", method: "POST" },
};

export async function runTesterTool(initData: string, tool: TesterTool): Promise<void> {
  const { path, method } = TESTER_REQUESTS[tool];
  await send(path, initData, method);
}

/** Calls the API, with an optional JSON body, and reads its JSON answer. */
async function request<T>(path: string, initData: string, method: Method = "GET", body?: unknown): Promise<T> {
  const response = await send(path, initData, method, body);
  return response.json();
}

/** Calls the API as the signed-in user. The backend checks the Telegram signature in the Authorization header. */
async function send(path: string, initData: string, method: Method, body?: unknown): Promise<Response> {
  const headers: Record<string, string> = { Authorization: `tma ${initData}` };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  const response = await fetch(path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
  });
  if (response.status === 401) throw new SignInError();
  if (!response.ok) throw new Error(`Backend answered ${response.status}`);
  return response;
}

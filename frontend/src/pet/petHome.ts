import type { DecorShop } from "../decor/decor";
import type { PetState } from "./petNeeds";
import type { Wallet } from "./wallet";

/** Everything the pet card starts from: the pet, the user's coins and the decor shop. */
export type PetHome = { pet: PetState; wallet: Wallet; decor: DecorShop };

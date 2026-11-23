import type { DecorShop } from "../decor/decor";
import type { PetNeeds } from "./petNeeds";
import type { Wallet } from "./wallet";

/** Everything the pet card starts from: the pet's needs, the user's coins and the decor shop. */
export type PetHome = { needs: PetNeeds; wallet: Wallet; decor: DecorShop };

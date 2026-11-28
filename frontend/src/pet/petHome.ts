import type { DecorShop } from "../decor/decor";
import type { FlowerId } from "../garden/garden";
import type { PetState } from "./petNeeds";
import type { Wallet } from "./wallet";

/** Everything the pet card starts from: the pet, the user's coins, the decor shop and the flower in the vase. */
export type PetHome = { pet: PetState; wallet: Wallet; decor: DecorShop; vase: FlowerId | null };

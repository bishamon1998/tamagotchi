import type { PetMood } from "./petNeeds";
import type { PetReactionKind } from "./usePet";

/** An animation a tester asks to see now: a reaction, or a mood held on screen (null goes back to the real one). */
export type PetDemoRequest = { kind: "reaction"; reaction: PetReactionKind } | { kind: "mood"; mood: PetMood | null };

/** A request with an id that changes every time, so pressing the same button again replays it. */
export type PetDemo = PetDemoRequest & { id: number };

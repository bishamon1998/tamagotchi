/** The pet's needs in whole percent, as the backend reports them. */
export type PetNeeds = { satiety: number; mood: number; energy: number };

/** Care actions the backend accepts. */
export type PetAction = "feed" | "play" | "sleep";

/** Below this a need is shown in the warning color. */
export const LOW_NEED = 30;

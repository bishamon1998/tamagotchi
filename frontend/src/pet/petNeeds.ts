/** The pet's needs in whole percent, as the backend reports them. */
export type PetNeeds = { satiety: number; mood: number; energy: number };

/**
 * The pet as the backend reports it: its needs, when it wakes up while it sleeps (ISO time, null when awake) and its
 * name (null until the user names it on arrival).
 */
export type PetState = PetNeeds & { name: string | null; asleepUntil: string | null };

/** Care actions the backend accepts. */
export type PetAction = "feed" | "treat" | "play" | "sleep";

/** The pet after a care action, the balance after it and the coins the action earned (0 if none). */
export type CareResult = PetState & { coins: number; earned: number };

/** Below this a need is shown in the warning color and the pet looks unwell. */
export const LOW_NEED = 30;

/** When every need is at least this, the pet wags its tail. */
export const HAPPY_NEED = 80;

/** How the pet looks between reactions. */
export type PetMood = "asleep" | "content" | "happy" | "hungry" | "tired" | "bored";

/** A sleeping pet just sleeps. Awake, the most pressing need wins: hunger first, then tiredness, then boredom. */
export function moodOf({ satiety, mood, energy, asleepUntil }: PetState): PetMood {
  if (asleepUntil) return "asleep";
  if (satiety < LOW_NEED) return "hungry";
  if (energy < LOW_NEED) return "tired";
  if (mood < LOW_NEED) return "bored";
  if (Math.min(satiety, mood, energy) >= HAPPY_NEED) return "happy";
  return "content";
}

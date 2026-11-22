export type PetStats = { satiety: number; mood: number; energy: number };

/** Pet needs in percent, shown until the backend keeps a real pet (next step of the plan). */
export const PREVIEW_STATS: PetStats = { satiety: 70, mood: 85, energy: 25 };

/** Below this a need is shown in the warning color. */
export const LOW_STAT = 30;

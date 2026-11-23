/** Taps on the pet within this window count as one burst. */
export const TOUCH_BURST_MS = 2000;

/** This many taps in one burst annoy the pet. */
export const TOUCHES_TO_ANNOY = 5;

/**
 * Remembers recent taps and tells whether the latest one annoys the pet. Keeps only the taps inside the burst
 * window and starts over after an annoyed reaction, so the pet is not annoyed by every tap that follows.
 */
export function createTouchCounter(now: () => number = Date.now) {
  let taps: number[] = [];
  return function touch(): "touch" | "annoyed" {
    const time = now();
    taps = taps.filter((tap) => time - tap < TOUCH_BURST_MS).concat(time);
    if (taps.length < TOUCHES_TO_ANNOY) return "touch";
    taps = [];
    return "annoyed";
  };
}

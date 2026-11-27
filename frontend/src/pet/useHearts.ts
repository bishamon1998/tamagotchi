import { useState } from "react";
import type { Heart } from "../components/pet/PetHearts";

/** A heart stays this long: its float animation, or a brief show without motion. */
const HEART_MS = 900;
/** At most this many hearts at once, so fast strokes do not flood the room. */
const MOST_HEARTS = 6;

/** Hearts floating up where the pet is stroked. `add` takes a point in CSS pixels inside the room. */
export function useHearts() {
  const [hearts, setHearts] = useState<Heart[]>([]);

  function add(x: number, y: number) {
    const heart = { id: Date.now() + Math.random(), x, y };
    setHearts((current) => [...current.slice(-(MOST_HEARTS - 1)), heart]);
    setTimeout(() => setHearts((current) => current.filter((other) => other.id !== heart.id)), HEART_MS);
  }

  return { hearts, add };
}

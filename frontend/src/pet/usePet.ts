import { useEffect, useRef, useState } from "react";
import { careForPet, fetchPet } from "../api";
import type { PetAction, PetState } from "./petNeeds";
import { createTouchCounter } from "./petTouch";

/** What the pet reacts to: a care action (going to sleep is a state, not a reaction) or a tap on the pet. */
export type PetReactionKind = Exclude<PetAction, "sleep"> | "touch" | "annoyed";

/** The pet's last reaction; the id changes every time so a repeat plays again. */
export type PetReaction = { kind: PetReactionKind; id: number };

/** Ask for the pet a moment after it should have woken up, so the backend already sees it awake. */
const WAKE_UP_MARGIN_MS = 1_000;

/**
 * The pet, a way to care for it and its reactions. One care action at a time, so quick taps do not pile up.
 * Each action reports the new balance and the coins it earned to `onCoins`. A sleeping pet is reloaded once it wakes.
 */
export function usePet(initData: string, initial: PetState, onCoins: (coins: number, earned: number) => void) {
  const [pet, setPet] = useState(initial);
  const [pending, setPending] = useState<PetAction | null>(null);
  const [failed, setFailed] = useState(false);
  const [reaction, setReaction] = useState<PetReaction | null>(null);
  const touchCounter = useRef(createTouchCounter());

  useEffect(() => {
    if (!pet.asleepUntil) return;
    const delay = Date.parse(pet.asleepUntil) - Date.now() + WAKE_UP_MARGIN_MS;
    const timer = setTimeout(() => {
      fetchPet(initData).then(setPet, (error) => console.error("Could not wake the pet up", error));
    }, Math.max(0, delay));
    return () => clearTimeout(timer);
  }, [initData, pet.asleepUntil]);

  const react = (kind: PetReactionKind) => setReaction((last) => ({ kind, id: (last?.id ?? 0) + 1 }));

  async function care(action: PetAction) {
    if (pending || pet.asleepUntil) return;
    setPending(action);
    setFailed(false);
    try {
      const { coins, earned, ...next } = await careForPet(initData, action);
      setPet(next);
      onCoins(coins, earned);
      if (action !== "sleep") react(action);
    } catch (error) {
      console.error("Care action failed", error);
      setFailed(true);
    } finally {
      setPending(null);
    }
  }

  /** A tap on the pet. Ignored while it sleeps or while a care reaction plays, so that is not cut short. */
  function touch() {
    if (pet.asleepUntil) return;
    if (reaction && reaction.kind !== "touch" && reaction.kind !== "annoyed") return;
    react(touchCounter.current());
  }

  const endReaction = () => setReaction(null);

  return { pet, pending, failed, reaction, care, touch, endReaction };
}

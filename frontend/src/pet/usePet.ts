import { useEffect, useRef, useState } from "react";
import { careForPet, fetchPet, namePet, strokePet } from "../api";
import type { PetAction, PetState } from "./petNeeds";
import { createTouchCounter } from "./petTouch";

/**
 * What the pet reacts to: a care action (going to sleep is a state, not a reaction), a tap on the pet, or being
 * stroked ("pet").
 */
export type PetReactionKind = Exclude<PetAction, "sleep"> | "touch" | "annoyed" | "pet";

/** The pet's last reaction; the id changes every time so a repeat plays again. */
export type PetReaction = { kind: PetReactionKind; id: number };

/** Strokes are sent to the backend at most this often: the same cooldown as PetRules.STROKE_COOLDOWN. */
const STROKE_COOLDOWN_MS = 30_000;

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
  const lastStrokeSent = useRef(0);

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

  /** Gives the pet a new name; fails (rejects) if the backend refuses it. */
  async function rename(name: string) {
    setPet(await namePet(initData, name));
  }

  /** A tap on the pet. Ignored while it sleeps or while a care reaction plays, so that is not cut short. */
  function touch() {
    if (pet.asleepUntil) return;
    if (reaction && reaction.kind !== "touch" && reaction.kind !== "annoyed") return;
    react(touchCounter.current());
  }

  /**
   * A stroke on the pet. It enjoys it unless it sleeps or is busy with care; a running stroke reaction is kept, not
   * restarted, so stroking does not stutter. Returns whether the pet took the stroke (and hearts should show).
   */
  function stroke(): boolean {
    if (pet.asleepUntil) return false;
    const kind = reaction?.kind;
    if (kind && kind !== "touch" && kind !== "annoyed" && kind !== "pet") return false;
    if (kind !== "pet") react("pet");
    sendStroke();
    return true;
  }

  /** Lets the backend count the stroke for mood, without asking more often than it would count one. */
  function sendStroke() {
    const now = Date.now();
    if (now - lastStrokeSent.current < STROKE_COOLDOWN_MS) return;
    lastStrokeSent.current = now;
    strokePet(initData).then(setPet, (error) => console.error("Stroke failed", error));
  }

  const endReaction = () => setReaction(null);

  return { pet, pending, failed, reaction, care, rename, touch, stroke, react, endReaction };
}

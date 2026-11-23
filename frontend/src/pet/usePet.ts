import { useRef, useState } from "react";
import { careForPet } from "../api";
import type { PetAction, PetNeeds } from "./petNeeds";
import { createTouchCounter } from "./petTouch";

/** What the pet reacts to: a care action or a tap on the pet. */
export type PetReactionKind = PetAction | "touch" | "annoyed";

/** The pet's last reaction; the id changes every time so a repeat plays again. */
export type PetReaction = { kind: PetReactionKind; id: number };

/** The pet's needs, a way to care for it and its reactions. One care action at a time, so quick taps do not pile up. */
export function usePet(initData: string, initial: PetNeeds) {
  const [needs, setNeeds] = useState(initial);
  const [pending, setPending] = useState<PetAction | null>(null);
  const [failed, setFailed] = useState(false);
  const [reaction, setReaction] = useState<PetReaction | null>(null);
  const touchCounter = useRef(createTouchCounter());

  const react = (kind: PetReactionKind) => setReaction((last) => ({ kind, id: (last?.id ?? 0) + 1 }));

  async function care(action: PetAction) {
    if (pending) return;
    setPending(action);
    setFailed(false);
    try {
      setNeeds(await careForPet(initData, action));
      react(action);
    } catch (error) {
      console.error("Care action failed", error);
      setFailed(true);
    } finally {
      setPending(null);
    }
  }

  /** A tap on the pet. Ignored while a care reaction plays, so it is not cut short. */
  function touch() {
    if (reaction && reaction.kind !== "touch" && reaction.kind !== "annoyed") return;
    react(touchCounter.current());
  }

  const endReaction = () => setReaction(null);

  return { needs, pending, failed, reaction, care, touch, endReaction };
}

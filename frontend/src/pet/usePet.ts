import { useState } from "react";
import { careForPet } from "../api";
import type { PetAction, PetNeeds } from "./petNeeds";

/** The pet's reaction to the last successful action; the id changes on every action so a repeat plays again. */
export type PetReaction = { action: PetAction; id: number };

/** The pet's needs and a way to care for it. One action at a time, so quick taps do not pile up. */
export function usePet(initData: string, initial: PetNeeds) {
  const [needs, setNeeds] = useState(initial);
  const [pending, setPending] = useState<PetAction | null>(null);
  const [failed, setFailed] = useState(false);
  const [reaction, setReaction] = useState<PetReaction | null>(null);

  async function care(action: PetAction) {
    if (pending) return;
    setPending(action);
    setFailed(false);
    try {
      setNeeds(await careForPet(initData, action));
      setReaction((last) => ({ action, id: (last?.id ?? 0) + 1 }));
    } catch (error) {
      console.error("Care action failed", error);
      setFailed(true);
    } finally {
      setPending(null);
    }
  }

  const endReaction = () => setReaction(null);

  return { needs, pending, failed, reaction, care, endReaction };
}

import { useEffect, useState } from "react";
import { buySeed, fetchGarden, pickFlower, plantSeed, putInVase, sellFlower, waterBed } from "../api";
import type { FlowerId, Garden, SeedKind } from "./garden";

/** The garden and its actions. One action at a time; each answer is the whole new garden. */
export function useGarden(initData: string) {
  const [garden, setGarden] = useState<Garden | null>(null);
  const [pending, setPending] = useState(false);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    fetchGarden(initData).then(setGarden, (error) => {
      console.error("Could not load the garden", error);
      setFailed(true);
    });
  }, [initData]);

  async function run(action: () => Promise<Garden>) {
    if (pending) return;
    setPending(true);
    setFailed(false);
    try {
      setGarden(await action());
    } catch (error) {
      console.error("Garden action failed", error);
      setFailed(true);
    } finally {
      setPending(false);
    }
  }

  return {
    garden,
    pending,
    failed,
    buy: (kind: SeedKind) => run(() => buySeed(initData, kind)),
    plant: (bed: number, kind: SeedKind) => run(() => plantSeed(initData, bed, kind)),
    water: (bed: number) => run(() => waterBed(initData, bed)),
    pick: (bed: number) => run(() => pickFlower(initData, bed)),
    sell: (flower: FlowerId) => run(() => sellFlower(initData, flower)),
    putInVase: (flower: FlowerId) => run(() => putInVase(initData, flower)),
  };
}

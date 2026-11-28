import background from "../assets/garden/garden.png";
import seed from "../assets/garden/stage-seed.png";
import sprout from "../assets/garden/stage-sprout.png";
import bud from "../assets/garden/stage-bud.png";
import wilting from "../assets/garden/stage-wilting.png";
import type { FlowerId, Growing } from "./garden";

export const gardenBackground = background;

const flowerArt = import.meta.glob<string>("../assets/garden/flowers/*.png", { eager: true, query: "?url", import: "default" });

/** The bloom picture of a flower: one 26x36 image per flower, named by its id. */
export function flowerImage(flower: FlowerId): string {
  return flowerArt[`../assets/garden/flowers/${flower}.png`];
}

/** What a plant looks like now: its stage, drooping when it wilts, or its own flower once it blooms. */
export function plantImage(plant: Growing): string {
  if (plant.flower) return flowerImage(plant.flower);
  if (plant.wilting) return wilting;
  return { seed, sprout, bud, bloom: bud }[plant.stage];
}

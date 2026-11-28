import type { Messages } from "../i18n/messages";
import type { Bed } from "./garden";

/** One line about a bed: empty, the plant's stage, wilting, or which flower bloomed. */
export function bedStatus(text: Messages["garden"], bed: Bed): string {
  const plant = bed.plant;
  if (!plant) return text.emptyBed;
  if (plant.flower) return text.bloomed(text.flowers[plant.flower]);
  if (plant.wilting) return text.wilting;
  return text.stages[plant.stage];
}

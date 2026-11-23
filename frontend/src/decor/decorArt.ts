import stripedRug from "../assets/decor/striped-rug.png";
import dogBed from "../assets/decor/dog-bed.png";
import ceilingLamp from "../assets/decor/ceiling-lamp.png";
import corgiPortrait from "../assets/decor/corgi-portrait.png";
import seaPicture from "../assets/decor/sea-picture.png";
import treat from "../assets/decor/treat.png";
import type { DecorItemId } from "./decor";

/** Pixel art for each decor item, drawn at its size in room pixels (positions are in RoomDecor.css). */
export const decorArt: Record<DecorItemId, string> = {
  "striped-rug": stripedRug,
  "dog-bed": dogBed,
  "ceiling-lamp": ceilingLamp,
  "corgi-portrait": corgiPortrait,
  "sea-picture": seaPicture,
};

/** The treat that lies in front of the pet while it eats one. */
export const treatArt = treat;

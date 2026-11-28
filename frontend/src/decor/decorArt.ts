import stripedRug from "../assets/decor/striped-rug.png";
import dogBed from "../assets/decor/dog-bed.png";
import ceilingLamp from "../assets/decor/ceiling-lamp.png";
import corgiPortrait from "../assets/decor/corgi-portrait.png";
import seaPicture from "../assets/decor/sea-picture.png";
import treat from "../assets/decor/treat.png";
import foodBowl from "../assets/decor/food-bowl.png";
import ball from "../assets/decor/ball.png";
import vase from "../assets/decor/vase.png";
import type { DecorItemId } from "./decor";

/** Pixel art for each decor item, drawn at its size in room pixels (positions are in RoomDecor.css). */
export const decorArt: Record<DecorItemId, string> = {
  "striped-rug": stripedRug,
  "dog-bed": dogBed,
  "ceiling-lamp": ceilingLamp,
  "corgi-portrait": corgiPortrait,
  "sea-picture": seaPicture,
};

/** Things that appear next to the pet while it is cared for: food, a treat or a toy. */
export type CareProp = "bowl" | "treat" | "ball";

export const careArt: Record<CareProp, string> = { bowl: foodBowl, treat, ball };

/** The floor vase that holds a flower from the garden. */
export const vaseArt = vase;

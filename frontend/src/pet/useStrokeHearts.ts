import { useRef } from "react";
import { useHearts } from "./useHearts";

/**
 * Hearts where the pet is stroked, wherever it is shown. Put `scene` on the element the hearts are drawn in and pass
 * `onStroke` to the PetSprite; `stroke` is the pet's own reaction, which says whether the pet took the stroke.
 */
export function useStrokeHearts<T extends HTMLElement>(stroke: () => boolean) {
  const scene = useRef<T>(null);
  const hearts = useHearts();
  const onStroke = (at: { x: number; y: number }) => {
    const area = scene.current?.getBoundingClientRect();
    if (area && stroke()) hearts.add(at.x - area.left, at.y - area.top);
  };
  return { scene, hearts: hearts.hearts, onStroke };
}

import "./PetHearts.css";
import type { CSSProperties } from "react";
import heart from "../../assets/effects/heart.png";

/** A heart that floats up from where the pet was stroked; x and y are CSS pixels inside the room. */
export type Heart = { id: number; x: number; y: number };

/** Hearts over the pet while it is stroked. Goes into PetScene's overlay, above the pet. */
export function PetHearts({ hearts }: { hearts: Heart[] }) {
  return (
    <>
      {hearts.map(({ id, x, y }) => (
        <img key={id} className="pet-heart" src={heart} alt="" style={{ "--x": `${x}px`, "--y": `${y}px` } as CSSProperties} />
      ))}
    </>
  );
}

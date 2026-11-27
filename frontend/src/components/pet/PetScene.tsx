import "./PetScene.css";
import type { ReactNode } from "react";

type Props = { night: boolean; decor: ReactNode; children: ReactNode };

/** The pet's room with its decor behind the pet. At night (while the pet sleeps) the lights go down. */
export function PetScene({ night, decor, children }: Props) {
  return (
    <div className={night ? "pet-scene pet-scene--night" : "pet-scene"}>
      <div className="pet-scene__decor" aria-hidden="true">{decor}</div>
      {children}
    </div>
  );
}

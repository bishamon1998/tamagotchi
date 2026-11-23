import "./PetScene.css";
import type { ReactNode } from "react";

/** The pet's room. At night (while the pet sleeps) the lights go down. */
export function PetScene({ night, children }: { night: boolean; children: ReactNode }) {
  return <div className={night ? "pet-scene pet-scene--night" : "pet-scene"}>{children}</div>;
}

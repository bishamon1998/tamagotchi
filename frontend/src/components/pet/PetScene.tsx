import "./PetScene.css";
import type { ReactNode, Ref } from "react";

type Props = {
  night: boolean;
  decor: ReactNode;
  /** Drawn above the pet, such as hearts while it is stroked. */
  overlay?: ReactNode;
  ref?: Ref<HTMLDivElement>;
  children: ReactNode;
};

/** The pet's room with its decor behind the pet. At night (while the pet sleeps) the lights go down. */
export function PetScene({ night, decor, overlay, ref, children }: Props) {
  return (
    <div ref={ref} className={night ? "pet-scene pet-scene--night" : "pet-scene"}>
      <div className="pet-scene__decor" aria-hidden="true">{decor}</div>
      {children}
      {overlay && <div className="pet-scene__overlay" aria-hidden="true">{overlay}</div>}
    </div>
  );
}

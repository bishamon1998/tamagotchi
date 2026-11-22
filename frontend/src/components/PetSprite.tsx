import "./PetSprite.css";

/** The pet drawn from its pixel-art sprite sheet, playing the idle animation (breathing, ear twitch, blink). */
export function PetSprite({ label }: { label: string }) {
  return <div className="pet-sprite" role="img" aria-label={label} />;
}

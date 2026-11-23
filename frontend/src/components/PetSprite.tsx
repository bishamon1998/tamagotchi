import "./PetSprite.css";
import type { AnimationEvent } from "react";
import type { PetAction } from "../pet/petNeeds";

type Props = { label: string; reaction?: PetAction | null; onReactionEnd?: () => void };

/**
 * The pet drawn from its pixel-art frames. Plays the idle animation (breathing, ear twitch, blink) or, after a care
 * action, a one-off reaction: chewing, hopping or sleeping. Remount it (change its key) to replay the same reaction.
 */
export function PetSprite({ label, reaction, onReactionEnd }: Props) {
  const className = reaction ? `pet-sprite pet-sprite--${reaction}` : "pet-sprite";
  const handleAnimationEnd = (event: AnimationEvent) => {
    if (reaction && event.target === event.currentTarget) onReactionEnd?.();
  };
  return <div className={className} role="img" aria-label={label} onAnimationEnd={handleAnimationEnd} />;
}

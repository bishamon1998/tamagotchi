import "./PetSprite.css";
import type { AnimationEvent } from "react";
import type { PetMood } from "../pet/petNeeds";
import type { PetReactionKind } from "../pet/usePet";
import { useStroke } from "../pet/useStroke";

type Props = {
  label: string;
  mood: PetMood;
  reaction?: PetReactionKind | null;
  onTouch: () => void;
  /** A stroke across the pet at a point on the screen (client coordinates); without it strokes are ignored. */
  onStroke?: (at: { x: number; y: number }) => void;
  onReactionEnd?: () => void;
};

const ignoreStroke = () => {};

/**
 * The pet drawn from its pixel-art frames, as a button the user can tap or stroke. Between reactions it shows its
 * mood: the idle loop (breathing, ear twitch, blink), a tail wag when all is well, a sad face for a low need, or sleep.
 * A reaction (chewing, hopping, a tap, being stroked) plays once on top. Remount it (change its key) to replay the
 * same reaction.
 */
export function PetSprite({ label, mood, reaction, onTouch, onStroke = ignoreStroke, onReactionEnd }: Props) {
  const stroke = useStroke(onStroke);
  const className = ["pet-sprite", `pet-sprite--${mood}`, reaction && `pet-sprite--react-${reaction}`]
    .filter(Boolean)
    .join(" ");
  // A reaction may run several animations at once (a tap: frames and a hop); it ends when the last one does.
  const handleAnimationEnd = (event: AnimationEvent<HTMLButtonElement>) => {
    if (reaction && event.target === event.currentTarget && event.currentTarget.getAnimations().length === 0) {
      onReactionEnd?.();
    }
  };
  const handleClick = () => {
    if (!stroke.endsStroke()) onTouch();
  };
  return (
    <button
      className={className}
      type="button"
      aria-label={label}
      onClick={handleClick}
      onAnimationEnd={handleAnimationEnd}
      {...stroke.handlers}
    />
  );
}

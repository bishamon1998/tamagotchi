import "./PetCard.css";
import type { Messages } from "../../i18n/messages";
import { moodOf, type PetNeeds } from "../../pet/petNeeds";
import { usePet } from "../../pet/usePet";
import { PetSprite } from "../PetSprite";
import { PetScene } from "./PetScene";
import { StatMeter } from "./StatMeter";
import { PetActions } from "./PetActions";

type Props = { initData: string; initialNeeds: PetNeeds; text: Messages };

/** The pet in its room with its needs and the care actions. */
export function PetCard({ initData, initialNeeds, text }: Props) {
  const { needs, pending, failed, reaction, care, touch, endReaction } = usePet(initData, initialNeeds);
  return (
    <section className="pet-card" aria-labelledby="pet-name">
      <PetScene night={reaction?.kind === "sleep"}>
        <PetSprite
          key={reaction?.id ?? 0}
          label={text.petTouch}
          mood={moodOf(needs)}
          reaction={reaction?.kind}
          onTouch={touch}
          onReactionEnd={endReaction}
        />
      </PetScene>
      <div className="pet-card__heading">
        <h2 className="pet-card__name" id="pet-name">{text.petName}</h2>
        <span className="pet-card__age">{text.petAge}</span>
      </div>
      <div className="pet-card__stats">
        <StatMeter label={text.stats.satiety} value={needs.satiety} />
        <StatMeter label={text.stats.mood} value={needs.mood} />
        <StatMeter label={text.stats.energy} value={needs.energy} />
      </div>
      <PetActions text={text} pending={pending} onCare={care} />
      {failed && <p className="pet-card__error" role="alert">{text.careFailed}</p>}
    </section>
  );
}

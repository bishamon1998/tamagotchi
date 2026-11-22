import "./PetCard.css";
import type { Messages } from "../../i18n/messages";
import type { PetStats } from "../../pet/previewStats";
import { PetSprite } from "../PetSprite";
import { StatMeter } from "./StatMeter";
import { PetActions } from "./PetActions";

/** The pet with its needs and the care actions. */
export function PetCard({ stats, text }: { stats: PetStats; text: Messages }) {
  return (
    <section className="pet-card" aria-labelledby="pet-name">
      <div className="pet-card__stage">
        <PetSprite label={text.petLabel} />
      </div>
      <div className="pet-card__heading">
        <h2 className="pet-card__name" id="pet-name">{text.petName}</h2>
        <span className="pet-card__age">{text.petAge}</span>
      </div>
      <div className="pet-card__stats">
        <StatMeter label={text.stats.satiety} value={stats.satiety} />
        <StatMeter label={text.stats.mood} value={stats.mood} />
        <StatMeter label={text.stats.energy} value={stats.energy} />
      </div>
      <PetActions text={text} />
    </section>
  );
}

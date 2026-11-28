import "./BedActions.css";
import type { Messages } from "../../i18n/messages";
import type { Bed, Garden, SeedKind } from "../../garden/garden";
import { bedStatus } from "../../garden/bedStatus";

type Text = Messages["garden"];
type Props = {
  text: Text;
  bed: Bed;
  seeds: Garden["seeds"];
  disabled: boolean;
  onPlant: (kind: SeedKind) => void;
  onWater: () => void;
  onPick: () => void;
};

/** What can be done with the selected bed: plant in an empty one, water a growing plant, pick a bloom. */
export function BedActions({ text, bed, seeds, disabled, onPlant, onWater, onPick }: Props) {
  const plant = bed.plant;
  return (
    <div className="bed-actions">
      <p className="bed-actions__status">{bedStatus(text, bed)}</p>
      {plant?.flower && <p className="bed-actions__fact">{text.facts[plant.flower]}</p>}
      <div className="bed-actions__buttons">
        {!plant && (
          <>
            <ActionButton
              label={text.plant.common(seeds.common)}
              disabled={disabled || seeds.common === 0}
              onClick={() => onPlant("common")}
            />
            <ActionButton
              label={text.plant.rare(seeds.rare)}
              disabled={disabled || seeds.rare === 0}
              onClick={() => onPlant("rare")}
            />
          </>
        )}
        {plant && plant.stage !== "bloom" && (
          <ActionButton
            label={plant.wateredToday ? text.watered : text.water}
            disabled={disabled || plant.wateredToday}
            onClick={onWater}
          />
        )}
        {plant?.stage === "bloom" && <ActionButton label={text.pick} disabled={disabled} onClick={onPick} />}
      </div>
    </div>
  );
}

function ActionButton({ label, disabled, onClick }: { label: string; disabled: boolean; onClick: () => void }) {
  return (
    <button className="bed-actions__button" type="button" disabled={disabled} onClick={onClick}>
      {label}
    </button>
  );
}

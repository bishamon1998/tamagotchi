import "./GardenScene.css";
import type { ReactNode } from "react";
import type { Bed } from "../../garden/garden";
import { plantImage } from "../../garden/gardenArt";

type Props = {
  beds: Bed[];
  selected: number | null;
  bedLabel: (bed: Bed) => string;
  onSelect: (bed: number) => void;
  /** The corgi, drawn on the lawn beside the beds. */
  pet: ReactNode;
};

/** The garden with its six beds and the corgi; each bed is a button that selects it and shows what grows in it. */
export function GardenScene({ beds, selected, bedLabel, onSelect, pet }: Props) {
  return (
    <div className="garden-scene">
      <div className="garden-scene__room">
        {beds.map((bed) => (
          <button
            key={bed.bed}
            className={bed.bed === selected ? "garden-bed garden-bed--selected" : "garden-bed"}
            type="button"
            aria-label={bedLabel(bed)}
            aria-pressed={bed.bed === selected}
            onClick={() => onSelect(bed.bed)}
          >
            {bed.plant && <img className="garden-bed__plant" src={plantImage(bed.plant)} alt="" />}
          </button>
        ))}
        {pet}
      </div>
    </div>
  );
}

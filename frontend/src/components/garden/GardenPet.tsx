import "./GardenPet.css";
import type { Messages } from "../../i18n/messages";
import { moodOf, type PetState } from "../../pet/petNeeds";
import { usePet } from "../../pet/usePet";
import { useStrokeHearts } from "../../pet/useStrokeHearts";
import { PetSprite } from "../PetSprite";
import { PetHearts } from "../pet/PetHearts";

type Props = { initData: string; pet: PetState; text: Messages };

const ignoreCoins = () => {};

/** The corgi keeps the user company in the garden: it sits on the lawn and can be tapped and stroked as at home. */
export function GardenPet({ initData, pet: initial, text }: Props) {
  const { pet, reaction, touch, stroke, endReaction } = usePet(initData, initial, ignoreCoins);
  const { scene, hearts, onStroke } = useStrokeHearts<HTMLDivElement>(stroke);
  return (
    <div ref={scene} className="garden-pet">
      <PetSprite
        key={reaction?.id ?? 0}
        label={text.petTouch}
        mood={moodOf(pet)}
        reaction={reaction?.kind}
        onTouch={touch}
        onStroke={onStroke}
        onReactionEnd={endReaction}
      />
      <div className="garden-pet__hearts" aria-hidden="true">
        <PetHearts hearts={hearts} />
      </div>
    </div>
  );
}

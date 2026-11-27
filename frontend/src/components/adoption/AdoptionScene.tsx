import "./AdoptionScene.css";
import { useState } from "react";
import closedBox from "../../assets/adoption/gift-box-closed.png";
import openBox from "../../assets/adoption/gift-box-open.png";
import type { Messages } from "../../i18n/messages";
import { PetSprite } from "../PetSprite";
import { PetScene } from "../pet/PetScene";
import { NameForm } from "../pet/NameForm";

type Props = { text: Messages["adoption"]; petTouch: string; onName: (name: string) => Promise<void> };

/**
 * The pet's arrival, shown until it has a name: a box in the room that wiggles until tapped, then the corgi hops
 * out and the user names it.
 */
export function AdoptionScene({ text, petTouch, onName }: Props) {
  const [opened, setOpened] = useState(false);
  // The corgi hops out of the box; each tap makes it hop again (a new key replays the reaction).
  const [hops, setHops] = useState(1);
  const [hopping, setHopping] = useState(true);
  const hop = () => {
    setHops((count) => count + 1);
    setHopping(true);
  };

  return (
    <section className="adoption" aria-labelledby="adoption-title">
      <PetScene night={false} decor={opened && <img className="adoption__open-box" src={openBox} alt="" />}>
        {opened ? (
          <PetSprite
            key={hops}
            label={petTouch}
            mood="happy"
            reaction={hopping ? "play" : null}
            onTouch={hop}
            onReactionEnd={() => setHopping(false)}
          />
        ) : (
          <button className="adoption__box" type="button" aria-label={text.open} onClick={() => setOpened(true)}>
            <img src={closedBox} alt="" />
          </button>
        )}
      </PetScene>
      <h2 className="adoption__title" id="adoption-title">{opened ? text.hello : text.arrived}</h2>
      {opened ? (
        <NameForm text={text.name} onSave={onName} />
      ) : (
        <button className="adoption__open" type="button" onClick={() => setOpened(true)}>{text.open}</button>
      )}
    </section>
  );
}

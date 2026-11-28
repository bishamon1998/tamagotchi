import "./FlowerRevealDialog.css";
import type { Messages } from "../../i18n/messages";
import { rarityOf, type FlowerId } from "../../garden/garden";
import { flowerImage } from "../../garden/gardenArt";
import { ModalDialog } from "../dialog/ModalDialog";

type Props = { flower: FlowerId | null; text: Messages["garden"]; onClose: () => void };

/** The flower the user just picked, shown up close with its name, rarity and a short note, before it goes in the basket. */
export function FlowerRevealDialog({ flower, text, onClose }: Props) {
  return (
    <ModalDialog open={flower !== null} title={flower ? text.flowers[flower] : ""} closeLabel={text.book.close} onClose={onClose}>
      {flower && (
        <div className="flower-reveal">
          <img className="flower-reveal__image" src={flowerImage(flower)} alt="" />
          <p className="flower-reveal__rarity">{text.rarity[rarityOf(flower)]}</p>
          <p className="flower-reveal__fact">{text.facts[flower]}</p>
          <button className="flower-reveal__done" type="button" onClick={onClose}>{text.toBasket}</button>
        </div>
      )}
    </ModalDialog>
  );
}

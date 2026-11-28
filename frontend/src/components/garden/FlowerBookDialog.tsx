import "./FlowerBookDialog.css";
import type { Messages } from "../../i18n/messages";
import { FLOWERS, type FlowerId } from "../../garden/garden";
import { flowerImage } from "../../garden/gardenArt";
import { ModalDialog } from "../dialog/ModalDialog";

type Props = { open: boolean; text: Messages["garden"]; discovered: FlowerId[]; onClose: () => void };

/**
 * The collection book: every flower with its name and a short note once the user has grown it, a dark shape with
 * "???" until then.
 */
export function FlowerBookDialog({ open, text, discovered, onClose }: Props) {
  const found = new Set(discovered);
  return (
    <ModalDialog open={open} title={text.book.title} closeLabel={text.book.close} onClose={onClose}>
      <ul className="flower-book">
        {FLOWERS.map(({ id, rarity }) => {
          const known = found.has(id);
          return (
            <li key={id} className={known ? "flower-book__item" : "flower-book__item flower-book__item--unknown"}>
              <img className="flower-book__image" src={flowerImage(id)} alt="" />
              <div className="flower-book__text">
                <span className="flower-book__name">{known ? text.flowers[id] : text.book.unknown}</span>
                <span className="flower-book__rarity">{text.rarity[rarity]}</span>
                {known && <p className="flower-book__fact">{text.facts[id]}</p>}
              </div>
            </li>
          );
        })}
      </ul>
    </ModalDialog>
  );
}

import "./RoomDecor.css";
import type { DecorItemId } from "../../decor/decor";
import { careArt, decorArt, type CareProp } from "../../decor/decorArt";

type Props = { items: DecorItemId[]; prop: CareProp | null };

/**
 * The decor placed in the room and, while the pet is cared for, the bowl, treat or ball it is busy with. Goes into
 * PetScene's decor layer.
 */
export function RoomDecor({ items, prop }: Props) {
  return (
    <>
      {items.map((item) => (
        <img key={item} className={`room-decor room-decor--${item}`} src={decorArt[item]} alt="" />
      ))}
      {prop && <img className={`room-decor room-decor--${prop}`} src={careArt[prop]} alt="" />}
    </>
  );
}

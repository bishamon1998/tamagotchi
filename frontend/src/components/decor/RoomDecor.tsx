import "./RoomDecor.css";
import type { DecorItemId } from "../../decor/decor";
import { decorArt, treatArt } from "../../decor/decorArt";

type Props = { items: DecorItemId[]; treat: boolean };

/** The decor placed in the room and, while the pet eats one, a treat on the floor. Goes into PetScene's decor layer. */
export function RoomDecor({ items, treat }: Props) {
  return (
    <>
      {items.map((item) => (
        <img key={item} className={`room-decor room-decor--${item}`} src={decorArt[item]} alt="" />
      ))}
      {treat && <img className="room-decor room-decor--treat" src={treatArt} alt="" />}
    </>
  );
}

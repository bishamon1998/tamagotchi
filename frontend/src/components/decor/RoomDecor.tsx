import "./RoomDecor.css";
import type { DecorItemId } from "../../decor/decor";
import { careArt, decorArt, vaseArt, type CareProp } from "../../decor/decorArt";
import type { FlowerId } from "../../garden/garden";
import { flowerImage } from "../../garden/gardenArt";

type Props = { items: DecorItemId[]; prop: CareProp | null; vase: FlowerId | null };

/**
 * The decor placed in the room, the vase with a flower from the garden, and, while the pet is cared for, the bowl,
 * treat or ball it is busy with. Goes into PetScene's decor layer.
 */
export function RoomDecor({ items, prop, vase }: Props) {
  return (
    <>
      {items.map((item) => (
        <img key={item} className={`room-decor room-decor--${item}`} src={decorArt[item]} alt="" />
      ))}
      {vase && (
        <>
          <img className="room-decor room-decor--vase-flower" src={flowerImage(vase)} alt="" />
          <img className="room-decor room-decor--vase" src={vaseArt} alt="" />
        </>
      )}
      {prop && <img className={`room-decor room-decor--${prop}`} src={careArt[prop]} alt="" />}
    </>
  );
}

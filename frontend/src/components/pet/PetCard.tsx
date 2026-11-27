import "./PetCard.css";
import { useState } from "react";
import type { Messages } from "../../i18n/messages";
import { moodOf } from "../../pet/petNeeds";
import type { PetHome } from "../../pet/petHome";
import { usePet } from "../../pet/usePet";
import { useCoins } from "../../pet/useCoins";
import { placedDecor } from "../../decor/decor";
import { useDecor } from "../../decor/useDecor";
import { PetSprite } from "../PetSprite";
import { CoinBalance } from "../coins/CoinBalance";
import { DailyBonus } from "../coins/DailyBonus";
import { RoomDecor } from "../decor/RoomDecor";
import { DecorShopDialog } from "../decor/DecorShopDialog";
import { PetScene } from "./PetScene";
import { StatMeter } from "./StatMeter";
import { PetActions } from "./PetActions";
import { SleepStatus } from "./SleepStatus";

type Props = { initData: string; home: PetHome; text: Messages };

/** The pet in its decorated room with its needs, the user's coins, the care actions and the decor shop. */
export function PetCard({ initData, home, text }: Props) {
  const { coins, gain, update: updateCoins } = useCoins(home.wallet.coins);
  const { pet, pending, failed, reaction, care, touch, endReaction } = usePet(initData, home.pet, updateCoins);
  const decor = useDecor(initData, home.decor.items, updateCoins);
  const [shopOpen, setShopOpen] = useState(false);
  const { dailyBonus, streak, treatPrice } = home.wallet;

  return (
    <section className="pet-card" aria-labelledby="pet-name">
      <PetScene night={pet.asleepUntil !== null} decor={<RoomDecor items={placedDecor(decor.items)} treat={reaction?.kind === "treat"} />}>
        <PetSprite
          key={reaction?.id ?? 0}
          label={text.petTouch}
          mood={moodOf(pet)}
          reaction={reaction?.kind}
          onTouch={touch}
          onReactionEnd={endReaction}
        />
      </PetScene>
      <div className="pet-card__heading">
        <h2 className="pet-card__name" id="pet-name">{text.petName}</h2>
        <span className="pet-card__age">{text.petAge}</span>
        <CoinBalance coins={coins} label={text.coins(coins)} gain={gain} />
      </div>
      {dailyBonus > 0 && <DailyBonus text={text.dailyBonus(dailyBonus, streak)} />}
      <div className="pet-card__stats">
        <StatMeter label={text.stats.satiety} value={pet.satiety} />
        <StatMeter label={text.stats.mood} value={pet.mood} />
        <StatMeter label={text.stats.energy} value={pet.energy} />
      </div>
      <PetActions text={text} pending={pending} asleep={pet.asleepUntil !== null} coins={coins} treatPrice={treatPrice} onCare={care} />
      {pet.asleepUntil && <SleepStatus asleepUntil={pet.asleepUntil} text={text.asleep} />}
      {failed && <p className="pet-card__error" role="alert">{text.careFailed}</p>}
      <button className="pet-card__shop" type="button" onClick={() => setShopOpen(true)}>{text.shop.open}</button>
      <DecorShopDialog
        open={shopOpen}
        text={text}
        coins={coins}
        items={decor.items}
        pending={decor.pending}
        failed={decor.failed}
        onBuy={decor.buy}
        onPlace={decor.place}
        onRemove={decor.remove}
        onClose={() => setShopOpen(false)}
      />
    </section>
  );
}

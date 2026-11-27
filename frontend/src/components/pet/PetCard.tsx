import "./PetCard.css";
import { useEffect, useRef, useState } from "react";
import type { Messages } from "../../i18n/messages";
import { moodOf, type PetMood } from "../../pet/petNeeds";
import type { PetDemo } from "../../pet/petDemo";
import type { PetHome } from "../../pet/petHome";
import { usePet, type PetReactionKind } from "../../pet/usePet";
import type { CareProp } from "../../decor/decorArt";
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
import { RenameDialog } from "./RenameDialog";
import { PetHearts } from "./PetHearts";
import { useHearts } from "../../pet/useHearts";
import { PencilIcon } from "../icons/PencilIcon";

/** `demo` is a tester's request to show an animation now (see TesterPanel). */
type Props = { initData: string; home: PetHome; text: Messages; demo?: PetDemo | null };

/** What lies next to the pet while it reacts to care: food, a treat or its ball. */
function careProp(reaction: PetReactionKind | undefined): CareProp | null {
  if (reaction === "feed") return "bowl";
  if (reaction === "treat") return "treat";
  if (reaction === "play") return "ball";
  return null;
}

/** The pet in its decorated room with its name, needs, the user's coins, the care actions and the decor shop. */
export function PetCard({ initData, home, text, demo = null }: Props) {
  const { coins, gain, update: updateCoins } = useCoins(home.wallet.coins);
  const { pet, pending, failed, reaction, care, rename, touch, stroke, react, endReaction } = usePet(
    initData,
    home.pet,
    updateCoins,
  );
  const scene = useRef<HTMLDivElement>(null);
  const hearts = useHearts();
  // A stroke the pet enjoys leaves a heart where the finger is.
  const strokePet = (at: { x: number; y: number }) => {
    const room = scene.current?.getBoundingClientRect();
    if (room && stroke()) hearts.add(at.x - room.left, at.y - room.top);
  };
  const decor = useDecor(initData, home.decor.items, updateCoins);
  const [shopOpen, setShopOpen] = useState(false);
  const [renameOpen, setRenameOpen] = useState(false);
  const name = pet.name ?? text.petName;
  // A mood a tester holds on screen; null shows the real one.
  const [demoMood, setDemoMood] = useState<PetMood | null>(null);
  const mood = demoMood ?? moodOf(pet);

  // Runs once per tester request; react changes every render, so only the request is a dependency.
  useEffect(() => {
    if (!demo) return;
    if (demo.kind === "reaction") react(demo.reaction);
    else setDemoMood(demo.mood);
  }, [demo]);
  const { dailyBonus, streak, treatPrice } = home.wallet;

  return (
    <section className="pet-card" aria-labelledby="pet-name">
      <PetScene
        ref={scene}
        night={mood === "asleep"}
        decor={<RoomDecor items={placedDecor(decor.items)} prop={careProp(reaction?.kind)} />}
        overlay={<PetHearts hearts={hearts.hearts} />}
      >
        <PetSprite
          key={reaction?.id ?? 0}
          label={text.petTouch}
          mood={mood}
          reaction={reaction?.kind}
          onTouch={touch}
          onStroke={strokePet}
          onReactionEnd={endReaction}
        />
      </PetScene>
      <div className="pet-card__heading">
        <h2 className="pet-card__name">
          <button className="pet-card__rename" type="button" onClick={() => setRenameOpen(true)}>
            <span id="pet-name">{name}</span>
            <PencilIcon />
            <span className="visually-hidden"> {text.rename.open}</span>
          </button>
        </h2>
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
      <RenameDialog
        open={renameOpen}
        text={text.rename}
        name={name}
        onSave={async (next) => {
          await rename(next);
          setRenameOpen(false);
        }}
        onClose={() => setRenameOpen(false)}
      />
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

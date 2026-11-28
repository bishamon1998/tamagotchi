import "./GardenCard.css";
import { useState } from "react";
import type { Messages } from "../../i18n/messages";
import { FLOWERS, type Bed, type FlowerId } from "../../garden/garden";
import { useGarden } from "../../garden/useGarden";
import { bedStatus } from "../../garden/bedStatus";
import type { PetState } from "../../pet/petNeeds";
import { CoinBalance } from "../coins/CoinBalance";
import { PixelStage } from "../PixelStage";
import { GardenScene } from "./GardenScene";
import { GardenPet } from "./GardenPet";
import { BedActions } from "./BedActions";
import { FlowerBookDialog } from "./FlowerBookDialog";
import { FlowerRevealDialog } from "./FlowerRevealDialog";

type Props = { initData: string; pet: PetState; text: Messages };

/**
 * The garden, where the corgi comes along: beds to plant, water every day and pick (a picked flower is shown up close
 * and goes in the basket for the market), and the book of flowers grown so far. Seeds are bought at the market.
 */
export function GardenCard({ initData, pet, text }: Props) {
  const { garden, pending, failed, plant, water, pick } = useGarden(initData);
  const [selected, setSelected] = useState<number | null>(null);
  const [bookOpen, setBookOpen] = useState(false);
  const [picked, setPicked] = useState<FlowerId | null>(null);
  const words = text.garden;

  if (!garden) {
    return (
      <section className="garden-card">
        <p className="garden-card__note" role={failed ? "alert" : "status"}>{failed ? words.failed : text.loading}</p>
      </section>
    );
  }

  const bed = selected === null ? null : garden.beds[selected];
  const bedLabel = (item: Bed) => `${words.bed(item.bed + 1)}: ${bedStatus(words, item)}`;
  const pickBed = (item: Bed) => {
    setPicked(item.plant?.flower ?? null);
    pick(item.bed);
  };

  return (
    <section className="garden-card" aria-labelledby="garden-title">
      <PixelStage>
        <GardenScene
          beds={garden.beds}
          selected={selected}
          bedLabel={bedLabel}
          onSelect={setSelected}
          pet={<GardenPet initData={initData} pet={pet} text={text} />}
        />
      </PixelStage>
      <div className="garden-card__heading">
        <h2 className="garden-card__title" id="garden-title">{words.title}</h2>
        <CoinBalance coins={garden.coins} label={text.coins(garden.coins)} />
      </div>
      {garden.freeSeed && <p className="garden-card__note" role="status">{words.freeSeed}</p>}
      {bed ? (
        <BedActions
          text={words}
          bed={bed}
          seeds={garden.seeds}
          disabled={pending}
          onPlant={(kind) => plant(bed.bed, kind)}
          onWater={() => water(bed.bed)}
          onPick={() => pickBed(bed)}
        />
      ) : (
        <p className="garden-card__note">{words.chooseBed}</p>
      )}
      {failed && <p className="garden-card__error" role="alert">{words.failed}</p>}
      <p className="garden-card__note">{words.seeds(garden.seeds.common, garden.seeds.rare)}</p>
      <button className="garden-card__book" type="button" onClick={() => setBookOpen(true)}>
        {words.book.open(garden.discovered.length, FLOWERS.length)}
      </button>
      <FlowerBookDialog open={bookOpen} text={words} discovered={garden.discovered} onClose={() => setBookOpen(false)} />
      <FlowerRevealDialog flower={picked} text={words} onClose={() => setPicked(null)} />
    </section>
  );
}

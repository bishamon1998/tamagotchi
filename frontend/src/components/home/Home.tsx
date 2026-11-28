import "./Home.css";
import { useState } from "react";
import { namePet } from "../../api";
import type { Messages } from "../../i18n/messages";
import type { PetHome } from "../../pet/petHome";
import type { PetDemo, PetDemoRequest } from "../../pet/petDemo";
import { AdoptionScene } from "../adoption/AdoptionScene";
import { PetCard } from "../pet/PetCard";
import { GamesSection } from "../games/GamesSection";
import { TesterPanel } from "../tester/TesterPanel";
import { GardenCard } from "../garden/GardenCard";
import { MarketCard } from "../market/MarketCard";
import { PlaceTabs, type Place } from "../places/PlaceTabs";

type Props = { name: string; tester: boolean; initData: string; pet: PetHome; text: Messages; onReload: () => void };

/**
 * Home screen: until the pet has a name, the scene where it arrives; then tabs for the places the user and the corgi
 * go (home, the garden, the market), the tester tools for testers and party games.
 */
export function Home({ name, tester, initData, pet, text, onReload }: Props) {
  const [demo, setDemo] = useState<PetDemo | null>(null);
  const [place, setPlace] = useState<Place>("home");
  // Tester tools change the game; away from home they reload just that place (a new key), so it stays open.
  const [placeVersion, setPlaceVersion] = useState(0);
  const showDemo = (request: PetDemoRequest) => setDemo((last) => ({ ...request, id: (last?.id ?? 0) + 1 }));

  // The garden and the market change coins and the vase, so coming home loads the room again.
  function go(next: Place) {
    if (next === place) return;
    if (next === "home") onReload();
    else setPlace(next);
  }

  async function adopt(petName: string) {
    await namePet(initData, petName);
    onReload();
  }

  if (pet.pet.name === null) {
    return (
      <main className="home">
        <h1 className="home__greeting">{text.greeting(name)}</h1>
        <AdoptionScene text={text.adoption} petTouch={text.petTouch} onName={adopt} />
      </main>
    );
  }

  return (
    <main className="home">
      <h1 className="home__greeting">{text.greeting(name)}</h1>
      <PlaceTabs current={place} text={text.places} onGo={go} />
      {place === "home" && <PetCard initData={initData} home={pet} text={text} demo={demo} />}
      {place === "garden" && <GardenCard key={placeVersion} initData={initData} pet={pet.pet} text={text} />}
      {place === "market" && <MarketCard key={placeVersion} initData={initData} text={text} />}
      {tester && (
        <TesterPanel
          initData={initData}
          text={text.tester}
          onDone={place === "home" ? onReload : () => setPlaceVersion((version) => version + 1)}
          onDemo={showDemo}
        />
      )}
      <GamesSection text={text} />
    </main>
  );
}

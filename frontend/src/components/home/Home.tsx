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

type Props = { name: string; tester: boolean; initData: string; pet: PetHome; text: Messages; onReload: () => void };

/**
 * Home screen: the pet on top (or, until it has a name, the scene where it arrives), then the tester tools for
 * testers, then party games.
 */
export function Home({ name, tester, initData, pet, text, onReload }: Props) {
  const [demo, setDemo] = useState<PetDemo | null>(null);
  const showDemo = (request: PetDemoRequest) => setDemo((last) => ({ ...request, id: (last?.id ?? 0) + 1 }));

  async function adopt(petName: string) {
    await namePet(initData, petName);
    onReload();
  }

  return (
    <main className="home">
      <h1 className="home__greeting">{text.greeting(name)}</h1>
      {pet.pet.name === null ? (
        <AdoptionScene text={text.adoption} petTouch={text.petTouch} onName={adopt} />
      ) : (
        <PetCard initData={initData} home={pet} text={text} demo={demo} />
      )}
      {tester && <TesterPanel initData={initData} text={text.tester} onDone={onReload} onDemo={showDemo} />}
      <GamesSection text={text} />
    </main>
  );
}

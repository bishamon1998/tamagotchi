import "./Home.css";
import type { Messages } from "../../i18n/messages";
import type { PetNeeds } from "../../pet/petNeeds";
import { PetCard } from "../pet/PetCard";
import { GamesSection } from "../games/GamesSection";

type Props = { name: string; initData: string; pet: PetNeeds; text: Messages };

/** Home screen: the pet on top, party games below. */
export function Home({ name, initData, pet, text }: Props) {
  return (
    <main className="home">
      <h1 className="home__greeting">{text.greeting(name)}</h1>
      <PetCard initData={initData} initialNeeds={pet} text={text} />
      <GamesSection text={text} />
    </main>
  );
}

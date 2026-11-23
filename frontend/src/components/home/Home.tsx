import "./Home.css";
import type { Messages } from "../../i18n/messages";
import type { PetHome } from "../../pet/petHome";
import { PetCard } from "../pet/PetCard";
import { GamesSection } from "../games/GamesSection";
import { TesterPanel } from "../tester/TesterPanel";

type Props = { name: string; tester: boolean; initData: string; pet: PetHome; text: Messages; onReload: () => void };

/** Home screen: the pet on top, then the tester tools for testers, then party games. */
export function Home({ name, tester, initData, pet, text, onReload }: Props) {
  return (
    <main className="home">
      <h1 className="home__greeting">{text.greeting(name)}</h1>
      <PetCard initData={initData} home={pet} text={text} />
      {tester && <TesterPanel initData={initData} text={text.tester} onDone={onReload} />}
      <GamesSection text={text} />
    </main>
  );
}

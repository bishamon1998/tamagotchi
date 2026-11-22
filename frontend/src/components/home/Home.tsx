import "./Home.css";
import type { Messages } from "../../i18n/messages";
import { PREVIEW_STATS } from "../../pet/previewStats";
import { PetCard } from "../pet/PetCard";
import { GamesSection } from "../games/GamesSection";

/** Home screen: the pet on top, party games below. */
export function Home({ name, text }: { name: string; text: Messages }) {
  return (
    <main className="home">
      <h1 className="home__greeting">{text.greeting(name)}</h1>
      <PetCard stats={PREVIEW_STATS} text={text} />
      <GamesSection text={text} />
    </main>
  );
}

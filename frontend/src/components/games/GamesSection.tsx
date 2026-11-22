import "./GamesSection.css";
import type { Messages } from "../../i18n/messages";
import { GameCard } from "./GameCard";
import { HatIcon } from "../icons/HatIcon";
import { DiceIcon } from "../icons/DiceIcon";

/** Party games for a Telegram group. None is playable yet. */
export function GamesSection({ text }: { text: Messages }) {
  return (
    <section className="games-section" aria-labelledby="games-title">
      <h2 className="games-section__title" id="games-title">{text.gamesTitle}</h2>
      <div className="games-section__list">
        <GameCard icon={<HatIcon />} title={text.hatTitle} description={text.hatDescription} badge={text.soon} />
        <GameCard icon={<DiceIcon />} title={text.moreGamesTitle} description={text.moreGamesDescription} />
      </div>
    </section>
  );
}

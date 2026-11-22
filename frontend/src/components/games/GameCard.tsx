import "./GameCard.css";
import type { ReactNode } from "react";

type Props = { icon: ReactNode; title: string; description: string; badge?: string };

/** One game in the list: icon, name, a line about it and an optional status badge. */
export function GameCard({ icon, title, description, badge }: Props) {
  return (
    <article className="game-card">
      <div className="game-card__icon">{icon}</div>
      <div className="game-card__body">
        <div className="game-card__heading">
          <h3 className="game-card__title">{title}</h3>
          {badge && <span className="game-card__badge">{badge}</span>}
        </div>
        <p className="game-card__description">{description}</p>
      </div>
    </article>
  );
}

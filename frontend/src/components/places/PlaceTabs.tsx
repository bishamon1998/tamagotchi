import "./PlaceTabs.css";
import type { Messages } from "../../i18n/messages";

export type Place = "home" | "garden" | "market";

const PLACES: Place[] = ["home", "garden", "market"];

type Props = { current: Place; text: Messages["places"]; onGo: (place: Place) => void };

/** Where the user and the corgi are: at home, in the garden or at the market. */
export function PlaceTabs({ current, text, onGo }: Props) {
  return (
    <nav className="place-tabs" aria-label={text.label}>
      {PLACES.map((place) => (
        <button
          key={place}
          className={place === current ? "place-tabs__tab place-tabs__tab--current" : "place-tabs__tab"}
          type="button"
          aria-current={place === current ? "page" : undefined}
          onClick={() => onGo(place)}
        >
          {text[place]}
        </button>
      ))}
    </nav>
  );
}

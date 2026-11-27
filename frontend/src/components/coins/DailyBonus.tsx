import "./DailyBonus.css";

/** A one-line note about the coins the day's first visit paid. */
export function DailyBonus({ text }: { text: string }) {
  return <p className="daily-bonus" role="status">{text}</p>;
}

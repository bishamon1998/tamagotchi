import "./SleepStatus.css";
import { useEffect, useState } from "react";

/** The countdown changes once a minute, so it is redrawn that often. */
const REFRESH_MS = 60_000;
const MS_PER_MINUTE = 60_000;

type Props = { asleepUntil: string; text: (minutesLeft: number) => string };

/** How long the pet will still sleep, rounded up to a minute. */
export function SleepStatus({ asleepUntil, text }: Props) {
  const [now, setNow] = useState(Date.now);

  useEffect(() => {
    const timer = setInterval(() => setNow(Date.now()), REFRESH_MS);
    return () => clearInterval(timer);
  }, []);

  const minutesLeft = Math.max(1, Math.ceil((Date.parse(asleepUntil) - now) / MS_PER_MINUTE));
  return <p className="sleep-status" role="status">{text(minutesLeft)}</p>;
}

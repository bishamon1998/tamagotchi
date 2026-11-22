import { Icon } from "./Icon";

/** Die, for games that are still to come. */
export function DiceIcon() {
  return (
    <Icon>
      <rect x="4" y="4" width="16" height="16" rx="3" />
      <circle cx="9" cy="9" r="0.6" fill="currentColor" />
      <circle cx="15" cy="15" r="0.6" fill="currentColor" />
      <circle cx="15" cy="9" r="0.6" fill="currentColor" />
      <circle cx="9" cy="15" r="0.6" fill="currentColor" />
    </Icon>
  );
}

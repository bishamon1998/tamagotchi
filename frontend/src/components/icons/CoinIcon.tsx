import { Icon } from "./Icon";

/** A coin, next to balances and prices. */
export function CoinIcon() {
  return (
    <Icon>
      <circle cx="12" cy="12" r="9" />
      <path d="M12 7.5v9" />
    </Icon>
  );
}

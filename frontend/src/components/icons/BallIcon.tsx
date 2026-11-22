import { Icon } from "./Icon";

/** Tennis ball, for playing with the pet. */
export function BallIcon() {
  return (
    <Icon>
      <circle cx="12" cy="12" r="9" />
      <path d="M5.6 5.6a9 9 0 0 1 0 12.8M18.4 5.6a9 9 0 0 0 0 12.8" />
    </Icon>
  );
}

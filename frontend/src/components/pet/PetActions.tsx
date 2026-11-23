import "./PetActions.css";
import type { ReactNode } from "react";
import type { Messages } from "../../i18n/messages";
import type { PetAction } from "../../pet/petNeeds";
import { BowlIcon } from "../icons/BowlIcon";
import { BoneIcon } from "../icons/BoneIcon";
import { BallIcon } from "../icons/BallIcon";
import { MoonIcon } from "../icons/MoonIcon";
import { CoinIcon } from "../icons/CoinIcon";

type Props = { text: Messages; pending: PetAction | null; coins: number; treatPrice: number; onCare: (action: PetAction) => void };

/** Care buttons. All are locked while one action is on its way to the server; a treat needs enough coins. */
export function PetActions({ text, pending, coins, treatPrice, onCare }: Props) {
  const button = (action: PetAction, label: string, icon: ReactNode) => (
    <ActionButton label={label} icon={icon} busy={pending === action} disabled={pending !== null} onClick={() => onCare(action)} />
  );
  return (
    <div className="pet-actions">
      {button("feed", text.actions.feed, <BowlIcon />)}
      <ActionButton
        label={text.actions.treat}
        ariaLabel={text.treatFor(treatPrice)}
        icon={<BoneIcon />}
        price={treatPrice}
        busy={pending === "treat"}
        locked={coins < treatPrice}
        disabled={pending !== null || coins < treatPrice}
        onClick={() => onCare("treat")}
      />
      {button("play", text.actions.play, <BallIcon />)}
      {button("sleep", text.actions.sleep, <MoonIcon />)}
    </div>
  );
}

type ButtonProps = {
  label: string;
  ariaLabel?: string;
  icon: ReactNode;
  price?: number;
  /** Not enough coins: shown greyed out. */
  locked?: boolean;
  busy: boolean;
  disabled: boolean;
  onClick: () => void;
};

function ActionButton({ label, ariaLabel, icon, price, locked, busy, disabled, onClick }: ButtonProps) {
  const className = locked ? "pet-actions__button pet-actions__button--locked" : "pet-actions__button";
  return (
    <button className={className} type="button" aria-label={ariaLabel} disabled={disabled} aria-busy={busy} onClick={onClick}>
      {icon}
      <span>{label}</span>
      {price !== undefined && (
        <span className="pet-actions__price">
          <CoinIcon />
          {price}
        </span>
      )}
    </button>
  );
}

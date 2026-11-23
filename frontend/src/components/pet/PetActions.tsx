import "./PetActions.css";
import type { ReactNode } from "react";
import type { Messages } from "../../i18n/messages";
import type { PetAction } from "../../pet/petNeeds";
import { BowlIcon } from "../icons/BowlIcon";
import { BallIcon } from "../icons/BallIcon";
import { MoonIcon } from "../icons/MoonIcon";

type Props = { text: Messages; pending: PetAction | null; onCare: (action: PetAction) => void };

/** Care buttons. All are locked while one action is on its way to the server. */
export function PetActions({ text, pending, onCare }: Props) {
  const button = (action: PetAction, label: string, icon: ReactNode) => (
    <ActionButton label={label} icon={icon} busy={pending === action} disabled={pending !== null} onClick={() => onCare(action)} />
  );
  return (
    <div className="pet-actions">
      {button("feed", text.actions.feed, <BowlIcon />)}
      {button("play", text.actions.play, <BallIcon />)}
      {button("sleep", text.actions.sleep, <MoonIcon />)}
    </div>
  );
}

type ButtonProps = { label: string; icon: ReactNode; busy: boolean; disabled: boolean; onClick: () => void };

function ActionButton({ label, icon, busy, disabled, onClick }: ButtonProps) {
  return (
    <button className="pet-actions__button" type="button" disabled={disabled} aria-busy={busy} onClick={onClick}>
      {icon}
      <span>{label}</span>
    </button>
  );
}

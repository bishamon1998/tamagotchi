import "./PetActions.css";
import type { ReactNode } from "react";
import type { Messages } from "../../i18n/messages";
import { BowlIcon } from "../icons/BowlIcon";
import { BallIcon } from "../icons/BallIcon";
import { MoonIcon } from "../icons/MoonIcon";

/** Care buttons. Disabled until the backend keeps the pet. */
export function PetActions({ text }: { text: Messages }) {
  return (
    <div className="pet-actions">
      <ActionButton label={text.actions.feed} icon={<BowlIcon />} />
      <ActionButton label={text.actions.play} icon={<BallIcon />} />
      <ActionButton label={text.actions.sleep} icon={<MoonIcon />} />
    </div>
  );
}

function ActionButton({ label, icon }: { label: string; icon: ReactNode }) {
  return (
    <button className="pet-actions__button" type="button" disabled>
      {icon}
      <span>{label}</span>
    </button>
  );
}

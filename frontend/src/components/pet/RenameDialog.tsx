import type { Messages } from "../../i18n/messages";
import { ModalDialog } from "../dialog/ModalDialog";
import { NameForm } from "./NameForm";

type Props = {
  open: boolean;
  text: Messages["rename"];
  name: string;
  onSave: (name: string) => Promise<void>;
  onClose: () => void;
};

/** Lets the user give the pet a new name. */
export function RenameDialog({ open, text, name, onSave, onClose }: Props) {
  return (
    <ModalDialog open={open} title={text.title} closeLabel={text.close} onClose={onClose}>
      <NameForm text={text.name} initial={name} onSave={onSave} />
    </ModalDialog>
  );
}

import "./ModalDialog.css";
import { useEffect, useId, useRef, type MouseEvent, type ReactNode } from "react";
import { CloseIcon } from "../icons/CloseIcon";

type Props = {
  open: boolean;
  title: string;
  closeLabel: string;
  /** Shown in the header between the title and the close button. */
  extra?: ReactNode;
  onClose: () => void;
  children: ReactNode;
};

/** A modal dialog with a title and a close button. Closes with Esc, the close button or a tap outside. */
export function ModalDialog({ open, title, closeLabel, extra, onClose, children }: Props) {
  const dialog = useRef<HTMLDialogElement>(null);
  const titleId = useId();

  useEffect(() => {
    const element = dialog.current;
    if (!element) return;
    if (open && !element.open) element.showModal();
    if (!open && element.open) element.close();
  }, [open]);

  // The dialog has no padding of its own, so a click that lands on the dialog itself is on the backdrop.
  const closeOnBackdrop = (event: MouseEvent<HTMLDialogElement>) => {
    if (event.target === event.currentTarget) onClose();
  };

  return (
    <dialog ref={dialog} className="modal-dialog" aria-labelledby={titleId} onClose={onClose} onClick={closeOnBackdrop}>
      <div className="modal-dialog__body">
        <header className="modal-dialog__header">
          <h2 className="modal-dialog__title" id={titleId}>{title}</h2>
          {extra}
          <button className="modal-dialog__close" type="button" aria-label={closeLabel} onClick={onClose}>
            <CloseIcon />
          </button>
        </header>
        {open && children}
      </div>
    </dialog>
  );
}

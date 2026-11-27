import "./NameForm.css";
import { useState, type FormEvent } from "react";
import { LONGEST_NAME } from "../../pet/petName";

type Text = { label: string; placeholder: string; save: string; failed: string };

type Props = {
  text: Text;
  initial?: string;
  /** Saves the name; a rejected promise shows the error and keeps the form open. */
  onSave: (name: string) => Promise<void>;
};

/** A text field for the pet's name and a save button. Blank names cannot be sent. */
export function NameForm({ text, initial = "", onSave }: Props) {
  const [name, setName] = useState(initial);
  const [saving, setSaving] = useState(false);
  const [failed, setFailed] = useState(false);
  const blank = name.trim() === "";

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (blank || saving) return;
    setSaving(true);
    setFailed(false);
    try {
      await onSave(name.trim());
    } catch (error) {
      console.error("Could not save the name", error);
      setFailed(true);
      setSaving(false);
    }
  }

  return (
    <form className="name-form" onSubmit={submit}>
      <input
        className="name-form__input"
        type="text"
        aria-label={text.label}
        placeholder={text.placeholder}
        value={name}
        maxLength={LONGEST_NAME}
        autoComplete="off"
        enterKeyHint="done"
        onChange={(event) => setName(event.target.value)}
      />
      <button className="name-form__save" type="submit" disabled={blank || saving} aria-busy={saving}>
        {text.save}
      </button>
      {failed && <p className="name-form__error" role="alert">{text.failed}</p>}
    </form>
  );
}

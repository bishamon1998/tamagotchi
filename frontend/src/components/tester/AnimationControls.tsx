import "./AnimationControls.css";
import type { Messages } from "../../i18n/messages";
import type { PetDemoRequest } from "../../pet/petDemo";
import type { PetMood } from "../../pet/petNeeds";
import type { PetReactionKind } from "../../pet/usePet";

type Text = Messages["tester"]["animations"];
type Props = { text: Text; onDemo: (request: PetDemoRequest) => void };

const REACTIONS: PetReactionKind[] = ["feed", "treat", "play", "touch", "annoyed"];
const MOODS: PetMood[] = ["content", "happy", "hungry", "tired", "bored", "asleep"];

/** Buttons that show any pet animation at once, so testers do not have to wait for it to happen. */
export function AnimationControls({ text, onDemo }: Props) {
  return (
    <div className="animation-controls">
      <h3 className="animation-controls__title">{text.title}</h3>
      <div className="animation-controls__group">
        {REACTIONS.map((reaction) => (
          <DemoButton key={reaction} label={text.reactions[reaction]} onClick={() => onDemo({ kind: "reaction", reaction })} />
        ))}
      </div>
      <div className="animation-controls__group">
        {MOODS.map((mood) => (
          <DemoButton key={mood} label={text.moods[mood]} onClick={() => onDemo({ kind: "mood", mood })} />
        ))}
        <DemoButton label={text.realMood} onClick={() => onDemo({ kind: "mood", mood: null })} />
      </div>
    </div>
  );
}

function DemoButton({ label, onClick }: { label: string; onClick: () => void }) {
  return (
    <button className="animation-controls__button" type="button" onClick={onClick}>
      {label}
    </button>
  );
}

import "./TesterPanel.css";
import { useState } from "react";
import { runTesterTool, type TesterTool } from "../../api";
import type { Messages } from "../../i18n/messages";
import type { PetDemoRequest } from "../../pet/petDemo";
import { AnimationControls } from "./AnimationControls";

type Props = {
  initData: string;
  text: Messages["tester"];
  onDone: () => void;
  onDemo: (request: PetDemoRequest) => void;
};

/** Remembers in this browser whether the tester keeps the panel open; a convenience only, so failures are ignored. */
const OPEN_KEY = "tester-panel-open";

function rememberedOpen(): boolean {
  try {
    return localStorage.getItem(OPEN_KEY) === "true";
  } catch {
    return false;
  }
}

function rememberOpen(open: boolean) {
  try {
    localStorage.setItem(OPEN_KEY, String(open));
  } catch {
    // Storage can be blocked; the panel then just starts folded.
  }
}

/**
 * Tools for testers to check the game without waiting: time and coins, the garden (seeds, instant blooms, every
 * flower) and animations. The place on screen is loaded again after each tool. The panel folds away when not needed.
 */
export function TesterPanel({ initData, text, onDone, onDemo }: Props) {
  const [pending, setPending] = useState<TesterTool | null>(null);
  const [failed, setFailed] = useState(false);

  async function run(tool: TesterTool) {
    setPending(tool);
    setFailed(false);
    try {
      await runTesterTool(initData, tool);
      onDone();
    } catch (error) {
      console.error("Tester tool failed", error);
      setFailed(true);
    } finally {
      setPending(null);
    }
  }

  const button = (tool: TesterTool, label: string) => (
    <button
      className="tester-panel__button"
      type="button"
      disabled={pending !== null}
      aria-busy={pending === tool}
      onClick={() => run(tool)}
    >
      {label}
    </button>
  );

  return (
    <details
      className="tester-panel"
      open={rememberedOpen()}
      onToggle={(event) => rememberOpen(event.currentTarget.open)}
    >
      <summary className="tester-panel__title">{text.title}</summary>
      <div className="tester-panel__body">
        <div className="tester-panel__tools">
          {button("coins", text.coins)}
          {button("skip-hours", text.skipHours)}
          {button("skip-day", text.skipDay)}
          {button("start-over", text.startOver)}
        </div>
        <h3 className="tester-panel__section">{text.gardenTools.title}</h3>
        <div className="tester-panel__tools">
          {button("seeds", text.gardenTools.seeds)}
          {button("bloom", text.gardenTools.bloom)}
          {button("flowers", text.gardenTools.flowers)}
        </div>
        <AnimationControls text={text.animations} onDemo={onDemo} />
        {failed && <p className="tester-panel__error" role="alert">{text.failed}</p>}
      </div>
    </details>
  );
}

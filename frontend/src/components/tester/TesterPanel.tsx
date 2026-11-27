import "./TesterPanel.css";
import { useState } from "react";
import { runTesterTool, type TesterTool } from "../../api";
import type { Messages } from "../../i18n/messages";

type Props = { initData: string; text: Messages["tester"]; onDone: () => void };

/** Tools for testers to check the game without waiting. After each one the pet is loaded again. */
export function TesterPanel({ initData, text, onDone }: Props) {
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
    <button className="tester-panel__button" type="button" disabled={pending !== null} aria-busy={pending === tool} onClick={() => run(tool)}>
      {label}
    </button>
  );

  return (
    <section className="tester-panel" aria-labelledby="tester-title">
      <h2 className="tester-panel__title" id="tester-title">{text.title}</h2>
      <div className="tester-panel__tools">
        {button("coins", text.coins)}
        {button("skip-hours", text.skipHours)}
        {button("skip-day", text.skipDay)}
        {button("start-over", text.startOver)}
      </div>
      {failed && <p className="tester-panel__error" role="alert">{text.failed}</p>}
    </section>
  );
}

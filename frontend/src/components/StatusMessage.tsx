import "./StatusMessage.css";

/** A single centered line for loading, errors and "open in Telegram". */
export function StatusMessage({ text }: { text: string }) {
  return (
    <main className="status-message" role="status">
      <p className="status-message__text">{text}</p>
    </main>
  );
}

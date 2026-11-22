import "./Greeting.css";

/** Home screen header until the pet arrives. */
export function Greeting({ title, subtitle }: { title: string; subtitle: string }) {
  return (
    <main className="greeting">
      <h1 className="greeting__title">{title}</h1>
      <p className="greeting__subtitle">{subtitle}</p>
    </main>
  );
}

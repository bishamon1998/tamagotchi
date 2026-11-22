import "./Greeting.css";
import { PetSprite } from "./PetSprite";

/** Home screen: a greeting and the pet. */
export function Greeting({ title, petLabel }: { title: string; petLabel: string }) {
  return (
    <main className="greeting">
      <h1 className="greeting__title">{title}</h1>
      <div className="greeting__pet">
        <PetSprite label={petLabel} />
      </div>
    </main>
  );
}

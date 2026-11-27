import { useCallback, useEffect, useState } from "react";
import { fetchMe, fetchPetHome, SignInError, type Me } from "./api";
import type { PetHome } from "./pet/petHome";
import { languageOf, messages } from "./i18n/messages";
import type { Launch } from "./telegram";
import { Home } from "./components/home/Home";
import { StatusMessage } from "./components/StatusMessage";

type State =
  | { kind: "loading" }
  | { kind: "ready"; me: Me; pet: PetHome; loadedAt: number }
  | { kind: "failed"; reason: "signIn" | "server" };

/** Signs the user in with Telegram launch data, loads their pet, coins and decor and shows the home screen. */
export function App({ launch }: { launch: Launch }) {
  const [state, setState] = useState<State>({ kind: "loading" });
  const language = state.kind === "ready" ? languageOf(state.me.language) : languageOf(launch?.languageCode ?? navigator.language);
  const text = messages[language];

  const load = useCallback(() => {
    if (!launch) return;
    Promise.all([fetchMe(launch.initData), fetchPetHome(launch.initData)])
      .then(([me, pet]) => setState({ kind: "ready", me, pet, loadedAt: Date.now() }))
      .catch((error) => setState({ kind: "failed", reason: error instanceof SignInError ? "signIn" : "server" }));
  }, [launch]);

  useEffect(() => {
    document.documentElement.lang = language;
  }, [language]);

  useEffect(load, [load]);

  if (!launch) return <StatusMessage text={text.openInTelegram} />;
  if (state.kind === "loading") return <StatusMessage text={text.loading} />;
  if (state.kind === "failed") {
    return <StatusMessage text={state.reason === "signIn" ? text.signInFailed : text.serverUnavailable} />;
  }
  return (
    <Home
      key={state.loadedAt}
      name={state.me.firstName}
      tester={state.me.tester}
      initData={launch.initData}
      pet={state.pet}
      text={text}
      onReload={load}
    />
  );
}

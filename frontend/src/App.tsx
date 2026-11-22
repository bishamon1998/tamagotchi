import { useEffect, useState } from "react";
import { fetchMe, SignInError, type Me } from "./api";
import { languageOf, messages } from "./i18n/messages";
import type { Launch } from "./telegram";
import { Greeting } from "./components/Greeting";
import { StatusMessage } from "./components/StatusMessage";

type State =
  | { kind: "loading" }
  | { kind: "ready"; me: Me }
  | { kind: "failed"; reason: "signIn" | "server" };

/** Signs the user in with Telegram launch data and shows their home screen. */
export function App({ launch }: { launch: Launch }) {
  const [state, setState] = useState<State>({ kind: "loading" });
  const language = state.kind === "ready" ? languageOf(state.me.language) : languageOf(launch?.languageCode ?? navigator.language);
  const text = messages[language];

  useEffect(() => {
    document.documentElement.lang = language;
  }, [language]);

  useEffect(() => {
    if (!launch) return;
    fetchMe(launch.initData)
      .then((me) => setState({ kind: "ready", me }))
      .catch((error) => setState({ kind: "failed", reason: error instanceof SignInError ? "signIn" : "server" }));
  }, [launch]);

  if (!launch) return <StatusMessage text={text.openInTelegram} />;
  if (state.kind === "loading") return <StatusMessage text={text.loading} />;
  if (state.kind === "failed") {
    return <StatusMessage text={state.reason === "signIn" ? text.signInFailed : text.serverUnavailable} />;
  }
  return <Greeting title={text.greeting(state.me.firstName)} subtitle={text.petSoon} />;
}

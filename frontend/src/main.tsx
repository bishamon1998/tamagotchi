import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "@fontsource-variable/nunito";
import "./styles/base.css";
import { App } from "./App";
import { startTelegram } from "./telegram";
import { preloadArt } from "./pet/preloadArt";

const launch = startTelegram();
preloadArt();

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App launch={launch} />
  </StrictMode>,
);

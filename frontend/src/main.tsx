import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "@fontsource-variable/nunito";
import "./styles/base.css";
import { App } from "./App";
import { startTelegram } from "./telegram";

const launch = startTelegram();

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <App launch={launch} />
  </StrictMode>,
);

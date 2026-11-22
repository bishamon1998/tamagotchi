import {
  bindThemeParamsCssVars,
  init,
  isTMA,
  miniAppReady,
  mountMiniAppSync,
  mountThemeParamsSync,
  retrieveLaunchParams,
  retrieveRawInitData,
} from "@telegram-apps/sdk-react";

/** What the page knows after starting: the signed launch data and the user's language, or nothing outside Telegram. */
export type Launch = { initData: string; languageCode: string | undefined } | null;

/** Connects to the Telegram client, applies its colors and tells it the app is ready. */
export function startTelegram(): Launch {
  if (!isTMA()) return null;
  try {
    init();
    mountThemeParamsSync();
    bindThemeParamsCssVars();
    mountMiniAppSync();
    miniAppReady();
  } catch (error) {
    // Launch parameters without a Telegram client around the page: treat it as opened outside Telegram.
    console.error("Telegram client is not available", error);
    return null;
  }
  const initData = retrieveRawInitData();
  if (!initData) return null;
  return { initData, languageCode: retrieveLaunchParams(true).tgWebAppData?.user?.languageCode };
}

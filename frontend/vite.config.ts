import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  build: {
    // Pixel art stays as separate cached files rather than data URIs: the app preloads it while the API answers
    // (src/pet/preloadArt.ts), and inlining would ship every frame twice, in the CSS and in the JS.
    assetsInlineLimit: 0,
  },
  server: {
    proxy: {
      "/api": "http://localhost:8080",
    },
  },
});

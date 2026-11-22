import express from "express";
import { createProxyMiddleware } from "http-proxy-middleware";
import { fileURLToPath } from "node:url";

const backend = new URL(process.env.BACKEND_UPSTREAM || "http://localhost:8080");
if (!["http:", "https:"].includes(backend.protocol) || backend.username || backend.password) {
  throw new Error("Invalid BACKEND_UPSTREAM");
}

const app = express();
app.disable("x-powered-by");
app.use((_request, response, next) => {
  response.set({
    "X-Content-Type-Options": "nosniff",
    "Referrer-Policy": "strict-origin-when-cross-origin",
    // Telegram Web shows Mini Apps in an iframe, so only its origin may frame the page.
    "Content-Security-Policy":
      "default-src 'self'; script-src 'self'; style-src 'self'; style-src-attr 'unsafe-inline'; img-src 'self' data:; connect-src 'self'; font-src 'self'; frame-ancestors 'self' https://web.telegram.org; base-uri 'self'",
  });
  next();
});
app.get("/healthz", (_request, response) => response.json({ status: "UP" }));

app.use(
  createProxyMiddleware({
    pathFilter: "/api",
    target: backend.href,
    changeOrigin: true,
    proxyTimeout: 5000,
    timeout: 10000,
    on: {
      error: (_error, _request, response) => {
        if ("writeHead" in response && !response.headersSent) {
          response.writeHead(502, { "Content-Type": "application/problem+json", "Cache-Control": "no-store" });
          response.end(JSON.stringify({ title: "Service unavailable", status: 502 }));
        }
      },
    },
  }),
);

const directory = fileURLToPath(new URL("./dist/", import.meta.url));
// Vite names built files by content hash, so browsers may keep them; index.html stays no-cache.
app.use("/assets", express.static(`${directory}assets`, { index: false, immutable: true, maxAge: "1y" }));
app.get("/", (_request, response) => {
  response.set("Cache-Control", "no-cache").sendFile(`${directory}index.html`);
});
app.use((_request, response) => response.status(404).json({ title: "Not found", status: 404 }));

const server = app.listen(Number(process.env.PORT || 3000), "0.0.0.0");
server.requestTimeout = 15000;
server.headersTimeout = 10000;
for (const signal of ["SIGTERM", "SIGINT"]) {
  process.on(signal, () => {
    server.close(() => process.exit(0));
    setTimeout(() => process.exit(1), 10000).unref();
  });
}

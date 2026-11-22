import type { ReactNode } from "react";

/** Shared frame for the line icons: 24x24, current text color, hidden from screen readers (buttons carry the text). */
export function Icon({ children }: { children: ReactNode }) {
  return (
    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth={2} strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
      {children}
    </svg>
  );
}

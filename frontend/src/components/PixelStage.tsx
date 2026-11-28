import "./PixelStage.css";
import { useCallback, type ReactNode } from "react";

/** Scenes are this many room pixels wide (the room and the garden). */
const SCENE_WIDTH = 160;
/** Above this many screen pixels per art pixel the scene stops growing, so it does not get blurry on wide screens. */
const LARGEST_SCALE = 3;

/**
 * The main game window: sets the pixel scale for the pixel-art scene inside so that it fills the stage's width, and
 * sits flush with the edges of its card. The scale may be fractional, so the scene uses all the room it has.
 */
export function PixelStage({ children }: { children: ReactNode }) {
  const fit = useCallback((stage: HTMLDivElement | null) => {
    if (!stage) return;
    const observer = new ResizeObserver(([entry]) => {
      const scale = Math.min(entry.contentRect.width / SCENE_WIDTH, LARGEST_SCALE);
      stage.style.setProperty("--pixel-scale", String(scale));
    });
    observer.observe(stage);
    return () => observer.disconnect();
  }, []);
  return (
    <div ref={fit} className="pixel-stage">
      {children}
    </div>
  );
}

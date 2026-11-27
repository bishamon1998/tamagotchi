import { useRef, type PointerEvent } from "react";

/** Moving this far with a finger or a pressed mouse button counts as one more stroke, in CSS pixels. */
const STROKE_STEP_PX = 24;

type Point = { x: number; y: number };

/**
 * Tells strokes from taps on the pet. While the pointer is pressed, every STROKE_STEP_PX of movement reports a stroke
 * at the pointer. A press that stroked does not also count as a tap: `endsStroke` answers that for the click after it.
 */
export function useStroke(onStroke: (at: Point) => void) {
  const last = useRef<Point | null>(null);
  const moved = useRef(0);
  const stroked = useRef(false);

  const start = (event: PointerEvent<HTMLElement>) => {
    last.current = { x: event.clientX, y: event.clientY };
    moved.current = 0;
    stroked.current = false;
    // Keep receiving moves when the finger slides off the pet mid-stroke.
    event.currentTarget.setPointerCapture(event.pointerId);
  };

  const move = (event: PointerEvent<HTMLElement>) => {
    if (!last.current) return;
    const point = { x: event.clientX, y: event.clientY };
    moved.current += Math.hypot(point.x - last.current.x, point.y - last.current.y);
    last.current = point;
    if (moved.current < STROKE_STEP_PX) return;
    moved.current = 0;
    stroked.current = true;
    onStroke(point);
  };

  const end = () => {
    last.current = null;
  };

  /** True once after a press that stroked, so its click is not also taken as a tap. */
  const endsStroke = () => {
    const was = stroked.current;
    stroked.current = false;
    return was;
  };

  return { handlers: { onPointerDown: start, onPointerMove: move, onPointerUp: end, onPointerCancel: end }, endsStroke };
}

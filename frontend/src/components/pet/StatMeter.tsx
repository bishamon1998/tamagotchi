import "./StatMeter.css";
import { LOW_NEED } from "../../pet/petNeeds";

/** One pet need as a labelled bar from 0 to 100. */
export function StatMeter({ label, value }: { label: string; value: number }) {
  const low = value < LOW_NEED;
  return (
    <div className="stat-meter">
      <span className="stat-meter__label">{label}</span>
      <div className="stat-meter__track" role="meter" aria-label={label} aria-valuemin={0} aria-valuemax={100} aria-valuenow={value}>
        <div className={low ? "stat-meter__fill stat-meter__fill--low" : "stat-meter__fill"} style={{ width: `${value}%` }} />
      </div>
    </div>
  );
}

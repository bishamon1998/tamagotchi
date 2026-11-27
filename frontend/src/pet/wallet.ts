/**
 * The user's coins as the backend reports them on loading. The backend counts coins; the app only shows them.
 * `dailyBonus` is what this visit paid (0 after the first visit of the day), `streak` is days in a row.
 */
export type Wallet = { coins: number; streak: number; dailyBonus: number; treatPrice: number };

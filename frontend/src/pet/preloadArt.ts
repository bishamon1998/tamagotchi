/**
 * Every pixel-art image the room can switch to: the pet's frames, the day and night room, the decor, the arrival box
 * and effects such as hearts. Animations swap whole images, so an image shown for the first time would be loaded and
 * decoded mid-animation and the first run would stutter.
 */
const ART_URLS = Object.values(
  import.meta.glob<string>(
    [
      "../assets/pet/*.png",
      "../assets/scene/*.png",
      "../assets/decor/*.png",
      "../assets/adoption/*.png",
      "../assets/effects/*.png",
      "../assets/garden/**/*.png",
    ],
    { eager: true, query: "?url", import: "default" },
  ),
);

/** Kept for the app's lifetime so the browser keeps the decoded images. */
const decodedArt: HTMLImageElement[] = [];

/** Loads and decodes all art up front, once, so every animation runs smoothly from its first frame. */
export function preloadArt() {
  if (decodedArt.length > 0) return;
  for (const url of ART_URLS) {
    const image = new Image();
    image.src = url;
    image.decode().catch((error) => console.warn("Could not preload", url, error));
    decodedArt.push(image);
  }
}

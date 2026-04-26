import { getStore } from "@netlify/blobs";

export default async (req) => {
  if (req.method !== "POST") {
    return new Response("Method not allowed", { status: 405 });
  }

  const store = getStore("jpfe-listings");
  const item = await req.json();

  const listings = (await store.get("items", { type: "json" })) || [];

  listings.unshift({
    id: Date.now(),
    ...item
  });

  await store.set("items", listings);

  return Response.json({ success: true });
};

import { getStore } from "@netlify/blobs";

export default async () => {
  const store = getStore("jpfe-listings");
  const listings = (await store.get("items", { type: "json" })) || [];
  return Response.json(listings);
};

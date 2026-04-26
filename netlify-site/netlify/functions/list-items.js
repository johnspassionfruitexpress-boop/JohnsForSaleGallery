import { getStore } from '@netlify/blobs';

export default async () => {
  const meta = getStore('for-sale-meta');
  const listed = await meta.list();
  const items = [];
  for (const blob of listed.blobs || []) {
    const txt = await meta.get(blob.key);
    if (txt) items.push(JSON.parse(txt));
  }
  items.sort((a,b)=> new Date(b.createdAt) - new Date(a.createdAt));
  return Response.json({ items });
};

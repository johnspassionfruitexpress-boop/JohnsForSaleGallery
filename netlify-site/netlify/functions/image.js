import { getStore } from '@netlify/blobs';

export default async (req) => {
  const url = new URL(req.url);
  const name = (url.searchParams.get('name') || '').replace(/[^a-zA-Z0-9_.-]/g, '_');
  if (!name) return new Response('Missing name', {status:400});
  const images = getStore('for-sale-images');
  const img = await images.get(name, { type: 'arrayBuffer' });
  if (!img) return new Response('Not found', {status:404});
  return new Response(img, { headers: { 'Content-Type':'image/jpeg', 'Cache-Control':'public, max-age=60' } });
};

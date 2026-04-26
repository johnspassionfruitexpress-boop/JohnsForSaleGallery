import { getStore } from '@netlify/blobs';

export default async (req) => {
  try {
    if (req.method !== 'POST') return Response.json({error:'POST only'}, {status:405});
    const data = await req.json();
    const needed = process.env.UPLOAD_PIN || '1234';
    if (!data.pin || data.pin !== needed) return Response.json({error:'Bad PIN'}, {status:401});
    if (!data.imageBase64 || !data.filename) return Response.json({error:'Missing image'}, {status:400});
    const clean = data.filename.replace(/[^a-zA-Z0-9_.-]/g, '_');
    const img = Buffer.from(data.imageBase64, 'base64');
    const images = getStore('for-sale-images');
    const meta = getStore('for-sale-meta');
    await images.set(clean, img, { metadata: { contentType: 'image/jpeg' } });
    const item = { filename: clean, type: data.type || 'Passion Fruit Plant', price: data.price || '25', createdAt: new Date().toISOString() };
    await meta.set(clean + '.json', JSON.stringify(item));
    return Response.json({ ok:true, item });
  } catch (e) {
    return Response.json({ error: e.message }, {status:500});
  }
};

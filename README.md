# Johns For Sale Gallery System

This repo has two parts:

1. `android-app/` — Android app that takes/picks plant photos, generates Facebook-ready JPG ads, saves them, and uploads them to your Netlify gallery.
2. `netlify-site/` — Netlify website with a For Sale Plants gallery powered by Netlify Functions + Blobs.

## GitHub APK build
Upload this whole repo to GitHub. Go to Actions > Build Android APK > Run workflow. Download the APK artifact.

## Netlify setup
Deploy the `netlify-site` folder to Netlify. In Netlify Site settings > Environment variables, add:

`UPLOAD_PIN=1234`

Use any PIN you want, but enter the same PIN in the Android app.

After deploy, copy your Netlify URL into the Android app, like:

`https://your-site.netlify.app`

Then Generate JPG > Upload to Website Gallery.

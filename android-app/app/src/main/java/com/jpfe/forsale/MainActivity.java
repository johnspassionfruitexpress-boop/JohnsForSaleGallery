package com.jpfe.forsale;

import android.app.*;
import android.os.*;
import android.provider.MediaStore;
import android.content.*;
import android.graphics.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.net.*;
import java.util.*;

public class MainActivity extends Activity {

    static final int REQ_PICK = 2;
    Bitmap selectedBitmap;

    ImageView preview;
    EditText price;
    TextView note;

    String cloudName = "dqdlmbj0q";
    String uploadPreset = "johns_passion_upload";

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30,30,30,30);
        setContentView(root);

        TextView title = new TextView(this);
        title.setText("Johns Passion Fruit Express\nFor Sale Generator");
        title.setTextSize(24);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        Button pick = new Button(this);
        pick.setText("Pick Plant Photo");
        root.addView(pick);

        preview = new ImageView(this);
        preview.setAdjustViewBounds(true);
        preview.setMaxHeight(700);
        root.addView(preview);

        price = new EditText(this);
        price.setHint("Price");
        price.setText("25");
        price.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        root.addView(price);

        Button upload = new Button(this);
        upload.setText("Upload to Cloudinary");
        root.addView(upload);

        note = new TextView(this);
        note.setText("Pick a plant photo, then upload. The app will create a Cloudinary image link.");
        root.addView(note);

        pick.setOnClickListener(v -> pickPhoto());
        upload.setOnClickListener(v -> uploadAd());
    }

    void pickPhoto() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQ_PICK);
    }

    @Override
    protected void onActivityResult(int r, int c, Intent data) {
        super.onActivityResult(r,c,data);

        if (r == REQ_PICK && c == RESULT_OK && data != null) {
            try {
                Uri uri = data.getData();
                selectedBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                preview.setImageBitmap(selectedBitmap);
            } catch(Exception e) {
                toast("Could not load image");
            }
        }
    }

    void uploadAd() {
        if (selectedBitmap == null) {
            toast("Pick a photo first");
            return;
        }

        note.setText("Uploading...");

        new Thread(() -> {
            try {
                String imageUrl = uploadBitmapToCloudinary(selectedBitmap);
                sendToNetlify(imageUrl);

                runOnUiThread(() -> {
                    note.setText(
                        "Upload complete!\n\n" +
                        "Image URL:\n" + imageUrl + "\n\n" +
                        "Copy this URL for now. Next step is saving this listing to the website gallery."
                    );
                    toast("Uploaded!");
                });

            } catch(Exception e) {
                runOnUiThread(() -> {
                    note.setText("Upload failed: " + e.getMessage());
                    toast("Upload failed");
                });
            }
        }).start();
    }

    String uploadBitmapToCloudinary(Bitmap bitmap) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
        byte[] imageBytes = bos.toByteArray();

        String boundary = "----JPFECloudinaryBoundary" + System.currentTimeMillis();
        URL url = new URL("https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

        DataOutputStream out = new DataOutputStream(conn.getOutputStream());

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"upload_preset\"\r\n\r\n");
        out.writeBytes(uploadPreset + "\r\n");

        out.writeBytes("--" + boundary + "\r\n");
        out.writeBytes("Content-Disposition: form-data; name=\"file\"; filename=\"plant.jpg\"\r\n");
        out.writeBytes("Content-Type: image/jpeg\r\n\r\n");
        out.write(imageBytes);
        out.writeBytes("\r\n");

        out.writeBytes("--" + boundary + "--\r\n");
        out.flush();
        out.close();

        int code = conn.getResponseCode();
        InputStream is = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();

        Scanner scanner = new Scanner(is).useDelimiter("\\A");
        String response = scanner.hasNext() ? scanner.next() : "";

        if (code < 200 || code >= 300) {
            throw new Exception(response);
        }

        String marker = "\"secure_url\":\"";
        int start = response.indexOf(marker);
        if (start == -1) throw new Exception("No secure_url returned");

        start += marker.length();
        int end = response.indexOf("\"", start);
        return response.substring(start, end).replace("\\/", "/");
    }
    void sendToNetlify(String imageUrl) {
    try {
        URL url = new URL("https://symphonious-sunflower-b8b0a0.netlify.app/.netlify/functions/add-listing");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        String json = "{"
                + "\"title\":\"Passion Fruit Plant\","
                + "\"price\":\"$" + price.getText().toString() + "\","
                + "\"description\":\"Healthy greenhouse-grown passion fruit plant. Guaranteed 1 ft+ at purchase.\","
                + "\"image\":\"" + imageUrl + "\""
                + "}";

        OutputStream os = conn.getOutputStream();
        os.write(json.getBytes());
        os.flush();
        os.close();

        int responseCode = conn.getResponseCode();

        runOnUiThread(() -> {
            toast("Saved to website! Code: " + responseCode);
        });

    } catch (Exception e) {
        runOnUiThread(() -> {
            toast("Netlify failed: " + e.getMessage());
        });
    }
}
    void toast(String s) {
        Toast.makeText(this, s, Toast.LENGTH_LONG).show();
    }
}

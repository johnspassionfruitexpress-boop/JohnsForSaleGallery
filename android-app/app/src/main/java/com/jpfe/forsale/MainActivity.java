package com.jpfe.forsale;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.provider.MediaStore;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.text.InputType;
import androidx.core.content.FileProvider;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.JSONObject;

public class MainActivity extends Activity {
    ImageView preview; Bitmap sourceBitmap, adBitmap; Uri photoUri; File photoFile;
    EditText price, siteUrl, pin; Spinner typeSpinner, styleSpinner; CheckBox showPrice, showPhone, showLocation, showGrade;
    String[] types = {"Passion Fruit Plant", "Purple Passion Fruit", "Yellow Passion Fruit", "Frederick Passion Fruit", "Possum Purple", "Red Rover", "Sweet Sunrise", "Other / Mixed"};
    int REQ_CAMERA=10, REQ_PICK=11;

    public void onCreate(Bundle b){ super.onCreate(b); requestPerms(); buildUi(); }
    void requestPerms(){ if(Build.VERSION.SDK_INT>=23) requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.INTERNET}, 2); }
    TextView tv(String t,int sp,int style){ TextView v=new TextView(this); v.setText(t); v.setTextSize(sp); v.setTypeface(Typeface.DEFAULT,style); v.setPadding(10,8,10,4); return v; }
    public void buildUi(){
        ScrollView sc=new ScrollView(this); LinearLayout root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(18,18,18,30); sc.addView(root);
        root.addView(tv("Johns Passion Fruit Express\nFor Sale Generator",24,Typeface.BOLD));
        preview=new ImageView(this); preview.setBackgroundColor(Color.rgb(235,245,235)); preview.setScaleType(ImageView.ScaleType.FIT_CENTER); root.addView(preview,new LinearLayout.LayoutParams(-1,900));
        LinearLayout row=new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); root.addView(row);
        Button cam=new Button(this); cam.setText("Take Photo"); row.addView(cam,new LinearLayout.LayoutParams(0,-2,1));
        Button pick=new Button(this); pick.setText("Pick Photo"); row.addView(pick,new LinearLayout.LayoutParams(0,-2,1));
        typeSpinner=new Spinner(this); typeSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, types)); root.addView(labelWrap("Passion fruit type", typeSpinner));
        price=new EditText(this); price.setHint("25"); price.setText("25"); price.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL); root.addView(labelWrap("Price", price));
        styleSpinner=new Spinner(this); styleSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"Clean Card", "Bold Marketplace", "Island Green"})); root.addView(labelWrap("Ad style", styleSpinner));
        showPrice=cb("Show price", true); showPhone=cb("Show phone", true); showLocation=cb("Show Oviedo", true); showGrade=cb("Show photo health grade", true); root.addView(showPrice); root.addView(showPhone); root.addView(showLocation); root.addView(showGrade);
        Button gen=new Button(this); gen.setText("Generate For Sale JPG Preview"); root.addView(gen);
        siteUrl=new EditText(this); siteUrl.setHint("https://your-site.netlify.app"); root.addView(labelWrap("Netlify site URL", siteUrl));
        pin=new EditText(this); pin.setHint("Upload PIN"); pin.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD); root.addView(labelWrap("Upload PIN", pin));
        Button save=new Button(this); save.setText("Save JPG to Phone"); root.addView(save);
        Button upload=new Button(this); upload.setText("Upload to Website Gallery"); root.addView(upload);
        TextView note=tv("Tip: Use vertical plant photos with the plant centered. The app auto-grades based on green/yellow/brown color and builds a Facebook-ready 4:5 ad image.",14,Typeface.NORMAL); root.addView(note);
        setContentView(sc);
        cam.setOnClickListener(v->takePhoto()); pick.setOnClickListener(v->pickPhoto()); gen.setOnClickListener(v->generateAd()); save.setOnClickListener(v->saveAd()); upload.setOnClickListener(v->uploadAd());
    }
    CheckBox cb(String t, boolean val){ CheckBox c=new CheckBox(this); c.setText(t); c.setTextSize(16); c.setChecked(val); return c; }
    LinearLayout labelWrap(String label, View child){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.addView(tv(label,14,Typeface.BOLD)); l.addView(child); return l; }
    void takePhoto(){ try{ photoFile=File.createTempFile("plant_", ".jpg", getExternalCacheDir()); photoUri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",photoFile); Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE); i.putExtra(MediaStore.EXTRA_OUTPUT, photoUri); startActivityForResult(i,REQ_CAMERA);}catch(Exception e){toast(e.toString());}}
    void pickPhoto(){ Intent i=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); startActivityForResult(i,REQ_PICK); }
    protected void onActivityResult(int r,int c,Intent data){ super.onActivityResult(r,c,data); if(c!=RESULT_OK)return; try{ Uri u=(r==REQ_CAMERA)?photoUri:data.getData(); sourceBitmap=MediaStore.Images.Media.getBitmap(getContentResolver(),u); sourceBitmap=forcePortrait(sourceBitmap); preview.setImageBitmap(sourceBitmap);}catch(Exception e){toast(e.toString());}}
    Bitmap forcePortrait(Bitmap b){ if(b.getWidth()>b.getHeight()){ Matrix m=new Matrix(); m.postRotate(90); return Bitmap.createBitmap(b,0,0,b.getWidth(),b.getHeight(),m,true);} return b; }
    void generateAd(){ if(sourceBitmap==null){toast("Take or pick a photo first"); return;} adBitmap=makeAd(sourceBitmap); preview.setImageBitmap(adBitmap); toast("Preview generated"); }
    Bitmap makeAd(Bitmap src){ int W=1080,H=1350; Bitmap out=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888); Canvas c=new Canvas(out); Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); p.setFilterBitmap(true); c.drawColor(Color.rgb(248,250,244));
        Bitmap crop=centerCrop(src,W,H); c.drawBitmap(crop,0,0,p);
        int style=styleSpinner.getSelectedItemPosition(); int topColor= style==1? Color.argb(230,20,70,25): style==2? Color.argb(225,16,92,58):Color.argb(220,255,255,255); int txt= style==0? Color.rgb(25,80,30):Color.WHITE;
        drawRound(c,35,35,W-35,275,32,topColor); drawRound(c,35,H-305,W-35,H-35,32,Color.argb(225,20,70,25));
        String analysis=analyze(src); String grade=analysis.split("\\|")[0]; String ratios=analysis.split("\\|")[1];
        drawText(c,"JOHNS PASSION FRUIT EXPRESS",58,95,34,txt,true); drawText(c,"Greenhouse Grown • Chemical Free • Island Love",58,142,24,txt,false);
        String title=types[typeSpinner.getSelectedItemPosition()]; drawText(c,title,58,205,42,txt,true);
        if(showPrice.isChecked()) badge(c,W-320,72,260,95,"$"+price.getText().toString()+" EACH",Color.rgb(255,210,55),Color.rgb(55,55,20));
        badge(c,58,H-282,250,82,"1FT+",Color.WHITE,Color.rgb(20,90,30)); badge(c,330,H-282,310,82,"HEALTHY",Color.WHITE,Color.rgb(20,90,30));
        if(showGrade.isChecked()) badge(c,665,H-282,325,82,"GRADE " + grade, Color.rgb(255,235,120), Color.rgb(55,55,20));
        drawText(c,"Ready to transplant or keep in pot",58,H-170,30,Color.WHITE,true); drawText(c,ratios,58,H-122,24,Color.WHITE,false);
        String bottom=""; if(showLocation.isChecked()) bottom+="Oviedo, FL"; if(showPhone.isChecked()) bottom+=(bottom.length()>0?"  •  ":"")+"407-902-8833"; drawText(c,bottom,58,H-70,28,Color.WHITE,true);
        return out; }
    Bitmap centerCrop(Bitmap src,int W,int H){ float scale=Math.max(W/(float)src.getWidth(),H/(float)src.getHeight()); int nw=Math.round(src.getWidth()*scale), nh=Math.round(src.getHeight()*scale); Bitmap scaled=Bitmap.createScaledBitmap(src,nw,nh,true); return Bitmap.createBitmap(scaled,(nw-W)/2,(nh-H)/2,W,H); }
    void drawRound(Canvas c,int l,int t,int r,int b,int rad,int color){ Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(color); c.drawRoundRect(new RectF(l,t,r,b),rad,rad,p); }
    void drawText(Canvas c,String s,int x,int y,int size,int color,boolean bold){ Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(color); p.setTextSize(size); p.setTypeface(Typeface.create(Typeface.DEFAULT,bold?Typeface.BOLD:Typeface.NORMAL)); c.drawText(s,x,y,p); }
    void badge(Canvas c,int x,int y,int w,int h,String s,int bg,int fg){ drawRound(c,x,y,x+w,y+h,25,bg); Paint p=new Paint(Paint.ANTI_ALIAS_FLAG); p.setColor(fg); p.setTypeface(Typeface.DEFAULT_BOLD); p.setTextSize(32); p.setTextAlign(Paint.Align.CENTER); Paint.FontMetrics fm=p.getFontMetrics(); c.drawText(s,x+w/2,y+h/2-(fm.ascent+fm.descent)/2,p); }
    String analyze(Bitmap b){ Bitmap small=Bitmap.createScaledBitmap(b,160,200,true); int green=0,yellow=0,brown=0,total=0; int[] pix=new int[small.getWidth()*small.getHeight()]; small.getPixels(pix,0,small.getWidth(),0,0,small.getWidth(),small.getHeight()); for(int col:pix){ int r=Color.red(col),g=Color.green(col),bl=Color.blue(col); if(g>55 && g>r*0.9 && g>bl*0.9){green++; total++;} else if(r>95 && g>80 && bl<95){yellow++; total++;} else if(r>65 && g>35 && bl<55 && r>g){brown++; total++;} }
        int denom=Math.max(total,1); int gp=green*100/denom, yp=yellow*100/denom, bp=brown*100/denom; String grade = gp>80 && bp<8?"A": gp>65?"B+": gp>45?"B":"C"; return grade+"|Photo Review: Green "+gp+"% • Yellow "+yp+"% • Brown "+bp+"%"; }
    void saveAd(){ if(adBitmap==null) generateAd(); if(adBitmap==null)return; try{ String name="JPFE_ForSale_"+new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(new Date())+".jpg"; ContentValues v=new ContentValues(); v.put(MediaStore.Images.Media.DISPLAY_NAME,name); v.put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg"); if(Build.VERSION.SDK_INT>=29) v.put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/JohnsPassionFruitAds"); Uri uri=getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v); OutputStream os=getContentResolver().openOutputStream(uri); adBitmap.compress(Bitmap.CompressFormat.JPEG,94,os); os.close(); toast("Saved: "+name);}catch(Exception e){toast(e.toString());}}
    void uploadAd(){ if(adBitmap==null) generateAd(); if(adBitmap==null)return; new Thread(()->{ try{ String base=siteUrl.getText().toString().trim(); if(!base.startsWith("http")) throw new Exception("Enter your Netlify site URL"); ByteArrayOutputStream baos=new ByteArrayOutputStream(); adBitmap.compress(Bitmap.CompressFormat.JPEG,90,baos); String b64=android.util.Base64.encodeToString(baos.toByteArray(),android.util.Base64.NO_WRAP); JSONObject obj=new JSONObject(); obj.put("pin",pin.getText().toString()); obj.put("filename","plant_"+System.currentTimeMillis()+".jpg"); obj.put("imageBase64",b64); obj.put("type",typeSpinner.getSelectedItem().toString()); obj.put("price",price.getText().toString()); URL url=new URL(base.replaceAll("/$","")+"/.netlify/functions/upload-item"); HttpURLConnection con=(HttpURLConnection)url.openConnection(); con.setRequestMethod("POST"); con.setDoOutput(true); con.setRequestProperty("Content-Type","application/json"); OutputStream os=con.getOutputStream(); os.write(obj.toString().getBytes("UTF-8")); os.close(); int code=con.getResponseCode(); runOnUiThread(()->toast(code==200?"Uploaded to website gallery":"Upload failed: "+code)); }catch(Exception e){ runOnUiThread(()->toast(e.getMessage())); }}).start(); }
    void toast(String s){ Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
}

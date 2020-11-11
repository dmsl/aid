package com.ucy.ecu.gui.aid;

import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaMetadata;
import android.os.Build;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import androidx.annotation.RequiresApi;

import com.ucy.ecu.gui.aid.BuildConfig;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

class MusicLibrary {
    private static final TreeMap<String, MediaMetadataCompat> music = new TreeMap<>();
    private static final TreeMap<String, MediaMetadataCompat> album = new TreeMap<>();

    private HashMap<String, LinkedList<MediaBrowserCompat.MediaItem>> mediaIdToChildren;

    public MusicLibrary(){
        mediaIdToChildren = new HashMap<String, LinkedList<MediaBrowserCompat.MediaItem>>();
        mediaIdToChildren.put("_Browsable_ROOT_", new LinkedList<MediaBrowserCompat.MediaItem>());
    }

    public void init(){
        LinkedList<MediaBrowserCompat.MediaItem> rootList = new LinkedList<>();
        MediaMetadataCompat selectedItems = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,"_ITEMS_")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,"Selected Items")
                .build();
        rootList.add(new MediaBrowserCompat.MediaItem(selectedItems.getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));

        MediaMetadataCompat allItems = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID,"_ALL_ITEMS_")
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE,"All Items")
                .build();
        rootList.add(new MediaBrowserCompat.MediaItem(allItems.getDescription(),MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        mediaIdToChildren.put("/",rootList);
        mediaIdToChildren.put("_ALL_ITEMS_",new LinkedList<MediaBrowserCompat.MediaItem>());
        mediaIdToChildren.put("_ITEMS_",new LinkedList<MediaBrowserCompat.MediaItem>());

    }

    public LinkedList<MediaBrowserCompat.MediaItem> getMedia(String parentId){
        return mediaIdToChildren.get(parentId);
    }


    public List<MediaBrowserCompat.MediaItem> getRootItems(){
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        result.add(new MediaBrowserCompat.MediaItem(album.get("Selected Values").getDescription(),
                MediaBrowserCompat.MediaItem.FLAG_BROWSABLE));
        return result;
    }

    public List<MediaBrowserCompat.MediaItem> getMediaItems() {
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();

        for (MediaMetadataCompat metadata: music.values()) {
            MediaBrowserCompat.MediaItem x=new MediaBrowserCompat.MediaItem(metadata.getDescription(),
                    MediaBrowserCompat.MediaItem.FLAG_PLAYABLE);
            result.add(x);

        }
        return result;
    }

    public LinkedList<MediaBrowserCompat.MediaItem> update(Context context, String parentMediaId,
                                                           String[] selected){

        DBHelper db = new DBHelper(context);
        ArrayList<String> array_list = db.getAllNames();
        ArrayList<Double> values=db.getAllVals();
        LinkedList<MediaBrowserCompat.MediaItem> mediaItems;
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(2);
        if(parentMediaId.equals("_ITEMS_")){
            mediaItems = new LinkedList<>();

            if((selected!=null)&&(selected.length!=0)){
                double value;
                int i=0;
                for (String it : selected) {
                    /*
                     * convert string to the database entry, meaning removing spaces for _ and first
                     * character is lowercase
                     */
                    String topic = it.replace(' ','_');
                    topic = Character.toLowerCase(topic.charAt(0)) + topic.substring(1);
                    value = db.getValue(topic);
                    String rounded = nf.format(value);
                    MediaMetadataCompat item = new MediaMetadataCompat.Builder()
                            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID,it)
                            .putString(MediaMetadata.METADATA_KEY_ARTIST,rounded)
                            .putLong(MediaMetadata.METADATA_KEY_DURATION, 100 * 1000)
                            .putString(MediaMetadata.METADATA_KEY_TITLE, it)
                            .build();
                    mediaItems.add(new MediaBrowserCompat.MediaItem(item.getDescription(),MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
                    i++;
                }
            }

        }else {
            mediaItems = new LinkedList<>();
            int i = 0;
            for (String names : array_list) {
                if(names.contains("PID")){
                    i++;
                    continue;
                }
                float val = values.get(i).floatValue();
                String rounded = nf.format(val);
                String formatted = names.replace('_',' ');
                formatted = Character.toUpperCase(formatted.charAt(0)) + formatted.substring(1);
                MediaMetadataCompat item = new MediaMetadataCompat.Builder()
                        .putString(MediaMetadata.METADATA_KEY_MEDIA_ID, formatted)
                        .putString(MediaMetadata.METADATA_KEY_ARTIST, rounded)
                        .putLong(MediaMetadata.METADATA_KEY_DURATION, 100 * 1000)
                        .putString(MediaMetadata.METADATA_KEY_TITLE, formatted)
                        .build();

                mediaItems.add(new MediaBrowserCompat.MediaItem(item.getDescription(), MediaBrowserCompat.MediaItem.FLAG_PLAYABLE));
                i++;
            }
        }
        return mediaItems;
    }
    public List<MediaBrowserCompat.MediaItem> getCombined(){
        List<MediaBrowserCompat.MediaItem> result = new ArrayList<>();
        result.addAll(getRootItems());
        result.addAll(getMediaItems());
        return result;
    }

    private static String getAlbumArtUri(String albumArtResName) {
        return ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                BuildConfig.APPLICATION_ID + "/drawable/" + albumArtResName;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void createMediaMetadata(Context context) {
        String filename="trial";
        String contents="";

        try {
            FileInputStream fis = context.openFileInput(filename);
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    line = reader.readLine();
                }
            } catch (IOException e) {
                // Error occurred when opening raw file for reading.
            } finally {
                contents = stringBuilder.toString();
            }
        }catch (Exception e){
            e.getMessage();
        }


        String[] split = contents.split("\n");
        int i=0;


        DBHelper db = new DBHelper(context);
        ArrayList<String> array_list = db.getAllNames();
        ArrayList<Double> values=db.getAllVals();

        for(String names : array_list){
            float val=values.get(i).floatValue();


            album.put("Selected Values",new MediaMetadataCompat.Builder()
                    .putString(MediaMetadata.METADATA_KEY_MEDIA_ID,"Selected Values")
                    .putString(MediaMetadata.METADATA_KEY_ALBUM,"Selected Values")
                    .build());

            if(names.contains("ambient_air_temperature")){
                album.put(names,
                        new MediaMetadataCompat.Builder()
                                .putString(MediaMetadata.METADATA_KEY_MEDIA_ID,"Selected Values")
                                .putString(MediaMetadata.METADATA_KEY_ALBUM,"Selected Values")
                                .putString(MediaMetadata.METADATA_KEY_ARTIST,""+val)
                                .putLong(MediaMetadata.METADATA_KEY_DURATION, 100 * 1000)
                                .putString(MediaMetadata.METADATA_KEY_TITLE, names)
                                .build());
            }
            music.put(names,
                    new MediaMetadataCompat.Builder()
                            .putString(MediaMetadata.METADATA_KEY_MEDIA_ID,names)
                            .putString(MediaMetadata.METADATA_KEY_ARTIST,""+val)
                            .putLong(MediaMetadata.METADATA_KEY_DURATION, 100 * 1000)
                            .putString(MediaMetadata.METADATA_KEY_TITLE, names)
                            .build());
            i++;
        }

    }


}

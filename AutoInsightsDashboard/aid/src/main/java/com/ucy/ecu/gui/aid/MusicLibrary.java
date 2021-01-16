package com.ucy.ecu.gui.aid;

import android.content.Context;
import android.media.MediaMetadata;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

class MusicLibrary {

    private HashMap<String, LinkedList<MediaBrowserCompat.MediaItem>> mediaIdToChildren;

    public MusicLibrary(){
        mediaIdToChildren = new HashMap<>();
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
                    String unit = db.getUnit(topic);
                    rounded = rounded + " " + unit;
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
                String unit = db.getUnit(names);
                rounded = rounded + " " + unit;
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

}

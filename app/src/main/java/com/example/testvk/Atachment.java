package com.example.testvk;

/**
 * Created by 95tox on 25.02.2016.
 */
import java.io.Serializable;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Atachment implements Serializable {
    private static final long serialVersionUID = 1L;
    public long id;//used only for wall post attached to message
    public String type; //photo,posted_photo,video,audio,link,note,app,poll,doc,geo,message,page,album
    public Photo photo;
    //public Photo posted_photo;
    public Video video;
    public WallMessage wallMessage;
    public VkPoll poll;

    public static ArrayList<Atachment> parseAttachments(JSONArray attachments, long from_id, long copy_owner_id, JSONObject geo_json) throws JSONException {
        ArrayList<Atachment> attachments_arr = new ArrayList<Atachment>();
        if (attachments != null) {
            int size = attachments.length();
            for (int j = 0; j < size; ++j) {
                Object att = attachments.get(j);
                if (att instanceof JSONObject == false)
                    continue;
                JSONObject json_attachment = (JSONObject) att;
                Atachment attachment = new Atachment();
                attachment.type = json_attachment.getString("type");
                if (attachment.type.equals("photo") || attachment.type.equals("posted_photo")) {
                    JSONObject x = json_attachment.optJSONObject("photo");
                    if (x != null)
                        attachment.photo = Photo.parse(x);
                } else if (attachment.type.equals("video"))
                    attachment.video = Video.parseForAttachments(json_attachment.getJSONObject("video"));
                else if (attachment.type.equals("poll")) {
                    attachment.poll = VkPoll.parse(json_attachment.getJSONObject("poll"));
                    if (attachment.poll.owner_id == 0) {
                        //это устарело потому что поля copy_owner_id больше нет при парсинге
                        //if(copy_owner_id!=0)
                        //    attachment.poll.owner_id=copy_owner_id;
                        //else
                        attachment.poll.owner_id = from_id;
                    }
                }
                //это устарело потому что поля copy_owner_id больше нет при парсинге
                //if(copy_owner_id!=0)
                //    attachment.poll.owner_id=copy_owner_id;
                //else
                if (attachments.equals("wall"))
                    attachment.wallMessage = WallMessage.parse(json_attachment.getJSONObject("wall"));
                attachments_arr.add(attachment);
            }

        }


        return attachments_arr;
    }


}

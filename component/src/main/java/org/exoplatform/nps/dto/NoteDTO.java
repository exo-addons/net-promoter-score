/*
 * Copyright (C) 2003-2016 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.nps.dto;


import java.util.ArrayList;

public class NoteDTO {


  private String              Id;

  private String              posterId;

  private long              postedTime;

  private String              activityId;

  private String              noteText;

  private String              posterName;
  private String              posterAvatar;
  private String             commentId;
  private ArrayList<NoteDTO>  notes;



  public String getId() {
    return Id;
  }

  public void setId(String id) {
    Id = id;
  }

  public String getPosterId() {
    return posterId;
  }

  public void setPosterId(String posterId) {
    this.posterId = posterId;
  }

  public long getPostedTime() {
    return postedTime;
  }

  public void setPostedTime(long postedTime) {
    this.postedTime = postedTime;
  }

  public String getActivityId() {
    return activityId;
  }

  public void setActivityId(String activityId) {
    this.activityId = activityId;
  }

  public String getNoteText() {
    return noteText;
  }

  public void setNoteText(String noteText) {
    this.noteText = noteText;
  }

  public String getPosterName() {
    return posterName;
  }

  public void setPosterName(String posterName) {
    this.posterName = posterName;
  }

  public String getPosterAvatar() {
    return posterAvatar;
  }

  public void setPosterAvatar(String posterAvatar) {
    this.posterAvatar = posterAvatar;
  }

  public ArrayList<NoteDTO> getNotes() {
    return notes;
  }

  public void setNotes(ArrayList<NoteDTO> notes) {
    this.notes = notes;
  }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public NoteDTO(String  Id, String posterId, long postedTime, String activityId, String noteText, String  posterName, String  posterAvatar, ArrayList<NoteDTO>  notes){
    this.Id=Id;
    this.posterId=posterId;
    this.postedTime=postedTime;
    this.activityId=activityId;
    this.noteText=noteText;
    this.posterName=posterName;
    this.posterAvatar=posterAvatar;
    this.notes=notes;
  }

  public NoteDTO(){

  }

}


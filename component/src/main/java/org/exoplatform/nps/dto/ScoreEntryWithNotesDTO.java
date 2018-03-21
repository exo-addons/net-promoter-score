package org.exoplatform.nps.dto;

import java.util.ArrayList;

public class ScoreEntryWithNotesDTO {
    private ScoreEntryDTO score;
    private ArrayList<NoteDTO> notes;

    public ScoreEntryDTO getScore() {
        return score;
    }

    public void setScore(ScoreEntryDTO score) {
        this.score = score;
    }

    public ArrayList<NoteDTO> getNotes() {
        return notes;
    }

    public void setNotes(ArrayList<NoteDTO> notes) {
        this.notes = notes;
    }


    public ScoreEntryWithNotesDTO(ScoreEntryDTO score, ArrayList<NoteDTO> notes){
        this.score=score;
        this.notes=notes;
    }
}

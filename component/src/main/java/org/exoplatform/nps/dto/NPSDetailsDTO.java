package org.exoplatform.nps.dto;

public class NPSDetailsDTO {

    private long typeId;
    private long npsFromDate;
    private long npsToDate;
    private long scorsnbr;
    private long detractorsNbr;
    private long promotersNbr;
    private long passivesNb;
    private float detractorsPrc;
    private float promotersPrc;
    private float passivesPrc;
    private float npScore;

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }

    public long getNpsFromDate() {
        return npsFromDate;
    }

    public void setNpsFromDate(long npsFromDate) {
        this.npsFromDate = npsFromDate;
    }

    public long getNpsToDate() {
        return npsToDate;
    }

    public void setNpsToDate(long npsToDate) {
        this.npsToDate = npsToDate;
    }

    public long getScorsnbr() {
        return scorsnbr;
    }

    public void setScorsnbr(long scorsnbr) {
        this.scorsnbr = scorsnbr;
    }

    public long getDetractorsNbr() {
        return detractorsNbr;
    }

    public void setDetractorsNbr(long detractorsNbr) {
        this.detractorsNbr = detractorsNbr;
    }

    public long getPromotersNbr() {
        return promotersNbr;
    }

    public void setPromotersNbr(long promotersNbr) {
        this.promotersNbr = promotersNbr;
    }

    public long getPassivesNb() {
        return passivesNb;
    }

    public void setPassivesNb(long passivesNb) {
        this.passivesNb = passivesNb;
    }

    public float getDetractorsPrc() {
        return detractorsPrc;
    }

    public void setDetractorsPrc(float detractorsPrc) {
        this.detractorsPrc = detractorsPrc;
    }

    public float getPromotersPrc() {
        return promotersPrc;
    }

    public void setPromotersPrc(float promotersPrc) {
        this.promotersPrc = promotersPrc;
    }

    public float getPassivesPrc() {
        return passivesPrc;
    }

    public void setPassivesPrc(float passivesPrc) {
        this.passivesPrc = passivesPrc;
    }

    public float getNpScore() {
        return npScore;
    }

    public void setNpScore(float npScore) {
        this.npScore = npScore;
    }

    public NPSDetailsDTO(long typeId,long npsFromDate,long npsToDate ,long scorsnbr,long detractorsNbr, long promotersNbr,long passivesNbr){
        this.typeId=typeId;
        this.npsFromDate=npsFromDate;
        this.npsToDate=npsToDate;
        this.scorsnbr=scorsnbr;
        this.detractorsNbr=detractorsNbr;
        this.promotersNbr=promotersNbr;
        this.passivesNb=passivesNbr;
        if(scorsnbr==0){
            this.detractorsPrc=0;
            this.promotersPrc=0;
            this.passivesPrc=0;
            this.npScore= 0;
        }else{
            this.detractorsPrc=((float)detractorsNbr/(float)scorsnbr)*100;
            this.promotersPrc=((float)promotersNbr/(float)scorsnbr)*100;
            this.passivesPrc=((float)passivesNbr/(float)scorsnbr)*100;
            this.npScore= this.promotersPrc-this.detractorsPrc;
        }

    }
}

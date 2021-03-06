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
package org.exoplatform.nps.entity;

import lombok.Data;
import org.exoplatform.commons.api.persistence.ExoEntity;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by The eXo Platform SAS
 */
@Entity(name = "NPSScoreEntryEntity")
@ExoEntity
@Table(name = "NPS_SCORE_ENTRY")
@NamedQueries({
        @NamedQuery(name = "scoreEntryEntity.findAllOrderByDesc", query = "SELECT a FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.postedTime > :startDate and a.postedTime < :endDate and a.responded = true order by a.id desc"),
        @NamedQuery(name = "scoreEntryEntity.findAllOrderByAsc", query = "SELECT a FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.responded = true  order by a.id asc"),
        @NamedQuery(name = "scoreEntryEntity.findAllPromoters", query = "SELECT a FROM NPSScoreEntryEntity a where a.typeId = :typeId  and a.score >= 9 and a.postedTime > :startDate and a.postedTime < :endDate and a.responded = true   order by a.id desc"),
        @NamedQuery(name = "scoreEntryEntity.findAllDetractors", query = "SELECT a FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.score <= 6 and a.postedTime > :startDate and a.postedTime < :endDate and a.responded = true   order by a.id desc"),
        @NamedQuery(name = "scoreEntryEntity.findAllPassives", query = "SELECT a FROM NPSScoreEntryEntity a where a.typeId = :typeId  and a.score < 9 and  a.score > 6 and a.postedTime > :startDate and a.postedTime < :endDate    and a.responded = true order by a.id desc"),
        @NamedQuery(name = "scoreEntryEntity.countAllresponded", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId  and a.postedTime > :startDate and a.postedTime < :endDate and a.responded = true "),
        @NamedQuery(name = "scoreEntryEntity.countAllPromoters", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId  and a.score >= 9 and a.postedTime > :startDate and a.postedTime < :endDate  and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.countAllDetractors", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.score <= 6 and a.postedTime > :startDate and a.postedTime < :endDate  and a.responded = true"),

        @NamedQuery(name = "scoreEntryEntity.countEnabledGroupdByScores", query = "SELECT a.score, count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.postedTime > :startDate and a.postedTime < :endDate and a.enabled = true   and a.responded = true group by a.score"),
        @NamedQuery(name = "scoreEntryEntity.findEnabledOrderByDesc", query = "SELECT a FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.enabled = true and a.postedTime > :startDate and a.postedTime < :endDate   and a.responded = true order by a.id desc"),
        @NamedQuery(name = "scoreEntryEntity.countEnabled", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.enabled = true and a.postedTime > :startDate and a.postedTime < :endDate  and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.countEnabledPromoters", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId  and a.score >= 9 and  a.enabled = true and a.postedTime > :startDate and a.postedTime < :endDate  and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.countEnabledDetractors", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.score <= 6 and  a.enabled = true and a.postedTime > :startDate and a.postedTime < :endDate  and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.countEnabledByDate", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.postedTime < :toDate and a.enabled = true and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.countEnabledPromotersByDate", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId  and a.score >= 9 and a.postedTime < :toDate and  a.enabled = true and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.countEnabledDetractorsByDate", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.score <= 6 and a.postedTime < :toDate and  a.enabled = true and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.countEnabledByPeriod", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.postedTime > :fromDate and a.postedTime < :toDate and a.enabled = true and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.countEnabledPromotersByPeriod", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId  and a.score >= 9 and a.postedTime > :fromDate and a.postedTime < :toDate and  a.enabled = true and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.countEnabledDetractorsByPeriod", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.score <= 6 and a.postedTime > :fromDate and a.postedTime < :toDate and  a.enabled = true and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.avgEnabled", query = "SELECT avg(a.score) FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.enabled = true and a.postedTime > :startDate and a.postedTime < :endDate and a.responded = true"),
        @NamedQuery(name = "scoreEntryEntity.findEnabledByUserId", query = "SELECT a FROM NPSScoreEntryEntity a where a.typeId = :typeId and a.userId = :userId  and a.responded = true order by a.id desc"),

        @NamedQuery(name = "scoreEntryEntity.countAll", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.typeId = :typeId  and a.postedTime > :startDate and a.postedTime < :endDate "),
        @NamedQuery(name = "scoreEntryEntity.findById", query = "SELECT a FROM NPSScoreEntryEntity a where a.id = :id") })


@Data
public class ScoreEntryEntity {

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEQ_NPS_SCORE_ENTRY_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_NPS_SCORE_ENTRY_ID")
  private long id;

  @Column(name = "USER_ID", nullable = false)
  private String userId;

  @Column(name = "SCORE")
  private int score;

  @Column(name = "POSTED_TIME")
  private long   postedTime;

  @Column(name = "LAST_APPEARENCE")
  private long lastAppereance;

  @Column(name = "COMMENT")
  private String comment;

  @Column(name = "ENABLED")
  private Boolean enabled;

  @Column(name = "TYPE_ID")
  private long   typeId;

  @Column(name = "RESPONDED")
  private Boolean   responded;

  @Column(name = "ACTIVITY_ID")
  private String   activityId;

  @Column(name = "SPACE_ID")
  private String   spaceId;

}
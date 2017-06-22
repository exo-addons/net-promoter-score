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
        @NamedQuery(name = "scoreEntryEntity.findAllOrderBy", query = "SELECT a FROM NPSScoreEntryEntity a order by a.id desc"),
        @NamedQuery(name = "scoreEntryEntity.count", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a"),
        @NamedQuery(name = "scoreEntryEntity.countPromoters", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.score >= 9"),
        @NamedQuery(name = "scoreEntryEntity.countDetractors", query = "SELECT count(a.id) FROM NPSScoreEntryEntity a where a.score <= 6"),
        @NamedQuery(name = "scoreEntryEntity.findByUserId", query = "SELECT a FROM NPSScoreEntryEntity a where a.userId = :userId order by a.id desc"),
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

}
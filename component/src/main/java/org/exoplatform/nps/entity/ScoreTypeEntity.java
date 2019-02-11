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

/**
 * Created by The eXo Platform SAS
 */
@Entity(name = "NPSScoreTypeEntity")
@ExoEntity
@Table(name = "NPS_SCORE_TYPE")
@NamedQueries({
        @NamedQuery(name = "scoreTypeEntity.findAllOrderBy", query = "SELECT a FROM NPSScoreTypeEntity a order by a.id desc"),
        @NamedQuery(name = "scoreTypeEntity.count", query = "SELECT count(a.id) FROM NPSScoreTypeEntity a"),
        @NamedQuery(name = "scoreTypeEntity.findById", query = "SELECT a FROM NPSScoreTypeEntity a where a.id = :id") })
@Data
public class ScoreTypeEntity {

  @Id
  @Column(name = "ID")
  @SequenceGenerator(name = "SEQ_NPS_SCORE_TYPE_ID")
  @GeneratedValue(strategy = GenerationType.AUTO, generator = "SEQ_NPS_SCORE_TYPE_ID")
  private long id;

  @Column(name = "TYPE_NAME", nullable = false)
  private String typeName;

  @Column(name = "QUESTION")
  private String question;

  @Column(name = "IS_DEFAULT")
  private Boolean   isDefault;


  @Column(name = "FOLLOWUP_DETRACTOR")
  private String followUpDetractor;


  @Column(name = "FOLLOWUP_PASSIVE")
  private String followUpPassive;


  @Column(name = "FOLLOWUP_PROMOTER")
  private String followUpPromoter;


  @Column(name = "ANONYMOUS")
  private Boolean  anonymous;

  @Column(name = "LINKED_TO_SPACE")
  private Boolean  linkedToSpace;

  @Column(name = "GAMIFIED")
  private Boolean  gamified;

  @Column(name = "SPACE_ID")
  private String spaceId;

  @Column(name = "USER_ID")
  private String userId;
}
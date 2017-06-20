/*
 *
 *  * Copyright (C) 2003-2016 eXo Platform SAS.
 *  *
 *  * This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Affero General Public License
 *  as published by the Free Software Foundation; either version 3
 *  of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see<http://www.gnu.org/licenses/>.
 *
 */
package org.exoplatform.nps.services;

import org.exoplatform.nps.dao.ScoreEntryDAO;
import org.exoplatform.nps.dto.ScoreEntryDTO;
import org.exoplatform.nps.entity.ScoreEntryEntity;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.log.ExoLogger;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by The eXo Platform SAS
 */
public class NpsService {
  private  final Log LOG = ExoLogger.getLogger(NpsService.class);


  private ScoreEntryDAO      scoreEntryDAO;


  public NpsService() {
    this.scoreEntryDAO = new ScoreEntryDAO();
  }

  public Boolean save(ScoreEntryDTO entity, boolean newEntry) {
    try {
      if (entity == null) {
        throw new IllegalStateException("Parameter 'entity' is null");
      }

      ScoreEntryEntity  scoreEntryEntity = null;
      if (newEntry) {
         entity.setPostedTime(System.currentTimeMillis());
         scoreEntryDAO.create(convert(entity));
       } else {
         scoreEntryDAO.update(convert(entity));
       }
    } catch (IllegalStateException e) {
      LOG.error("Cannot save the score", e.getMessage());
      return false;
    }
    return true;
  }

  public void remove(ScoreEntryDTO entity) {
   if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' = + "+entity+ " or 'entity.id' is null");
    }
    scoreEntryDAO.delete(convert(entity));
  }

  public List<ScoreEntryDTO> getScores(int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getScores - Parameter 'offset' must be positive");
    }
    List<ScoreEntryEntity> entities = scoreEntryDAO.getScoreEntries(offset, limit);
    List<ScoreEntryDTO> dtos = new ArrayList<ScoreEntryDTO>();
    for (ScoreEntryEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }



  public ScoreEntryDTO getScoreEntry(long id) {
    List<ScoreEntryEntity> entities=scoreEntryDAO.getScoreEntrybyId(id);
    if (entities.size()!=0){
      return convert(entities.get(0));
    }
    return null;
  }


  public List<ScoreEntryDTO> getScoreEntriesByUserId(String userId,  int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getScoreEntriesByUserId - Parameter 'offset' must be positive");
    }
    List<ScoreEntryEntity> entities = scoreEntryDAO.getScoreEntriesByUserId(userId,offset, limit);
    List<ScoreEntryDTO> dtos = new ArrayList<ScoreEntryDTO>();
    for (ScoreEntryEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }



  public long getPromotersCount() {

    return scoreEntryDAO.getPromotersCount();
  }


  public long getDetractorsCount() {

    return scoreEntryDAO.getDetractorsCount();
  }



  public long getScoreCount() {
    return scoreEntryDAO.getScoreEntriesCount();
  }


  private ScoreEntryEntity convert(ScoreEntryDTO dto) {
    ScoreEntryEntity entity = new ScoreEntryEntity();
    entity.setId(dto.getId());
    entity.setUserId(dto.getUserId());
    entity.setScore(dto.getScore());
    entity.setPostedTime(dto.getPostedTime());
    entity.setComment(dto.getComment());
    entity.setLastAppereance(dto.getLastAppereance());

    return entity;
  }

  private ScoreEntryDTO convert(ScoreEntryEntity entity) {
    ScoreEntryDTO dto = new ScoreEntryDTO();
    dto.setId(entity.getId());
    dto.setUserId(entity.getUserId());
    dto.setScore(entity.getScore());
    dto.setPostedTime(entity.getPostedTime());
    dto.setComment(entity.getComment());
    dto.setLastAppereance(entity.getLastAppereance());

    return dto;
  }

}

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

import org.apache.commons.collections.map.LinkedMap;
import org.eclipse.jetty.util.ajax.JSON;
import org.exoplatform.nps.dao.ScoreEntryDAO;
import org.exoplatform.nps.dto.ScoreEntryDTO;
import org.exoplatform.nps.entity.ScoreEntryEntity;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.log.ExoLogger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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

  public List<ScoreEntryDTO> getScores(long typeId, int offset, int limit, long startDate, long endDate) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getScores - Parameter 'offset' must be positive");
    }
    List<ScoreEntryEntity> entities = scoreEntryDAO.getAllScoreEntries(typeId, offset, limit, startDate, endDate);
    List<ScoreEntryDTO> dtos = new ArrayList<ScoreEntryDTO>();
    for (ScoreEntryEntity entity : entities) {
      if(entity.getEnabled()==null) entity.setEnabled(true);
      dtos.add(convert(entity));
    }
    return dtos;
  }

  public List<ScoreEntryDTO> getPromotesScores(long typeId, int offset, int limit, long startDate, long endDate) {

    if (offset < 0) {
      throw new IllegalArgumentException("Method getScores - Parameter 'offset' must be positive");
    }
    List<ScoreEntryEntity> entities = scoreEntryDAO.getAllPromoterScoreEntries(typeId, offset, limit, startDate, endDate);
    List<ScoreEntryDTO> dtos = new ArrayList<ScoreEntryDTO>();
    for (ScoreEntryEntity entity : entities) {
      if(entity.getEnabled()==null) entity.setEnabled(true);
      dtos.add(convert(entity));
    }
    return dtos;
  }


  public List<ScoreEntryDTO> getDetractorScores(long typeId, int offset, int limit, long startDate, long endDate){

    if (offset < 0) {
      throw new IllegalArgumentException("Method getScores - Parameter 'offset' must be positive");
    }
    List<ScoreEntryEntity> entities = scoreEntryDAO.getAllDetractorScoreEntries(typeId, offset, limit, startDate, endDate);
    List<ScoreEntryDTO> dtos = new ArrayList<ScoreEntryDTO>();
    for (ScoreEntryEntity entity : entities) {
      if(entity.getEnabled()==null) entity.setEnabled(true);
      dtos.add(convert(entity));
    }
    return dtos;
  }


  public List<ScoreEntryDTO> getPassiveScores(long typeId, int offset, int limit, long startDate, long endDate) {

    if (offset < 0) {
      throw new IllegalArgumentException("Method getScores - Parameter 'offset' must be positive");
    }
    List<ScoreEntryEntity> entities = scoreEntryDAO.getAllPassiveScoreEntries(typeId, offset, limit, startDate, endDate);
    List<ScoreEntryDTO> dtos = new ArrayList<ScoreEntryDTO>();
    for (ScoreEntryEntity entity : entities) {
      if(entity.getEnabled()==null) entity.setEnabled(true);
      dtos.add(convert(entity));
    }
    return dtos;
  }


  public ScoreEntryDTO getFirstScoreEntries(long typeId) {
    List<ScoreEntryEntity> entities =  scoreEntryDAO.getFirstScoreEntries(typeId);
    if( entities.size()>0){
      return convert(entities.get(0));
    } else return null;
  }


  public ScoreEntryDTO getScoreEntry(long id) {
    List<ScoreEntryEntity> entities=scoreEntryDAO.getScoreEntrybyId(id);
    if (entities.size()!=0){
      return convert(entities.get(0));
    }
    return null;
  }


  public List<ScoreEntryDTO> getScoreEntriesByUserId(long typeId, String userId,  int offset, int limit, long startDate, long endDate) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getScoreEntriesByUserId - Parameter 'offset' must be positive");
    }
    List<ScoreEntryEntity> entities = scoreEntryDAO.getScoreEntriesByUserId(typeId,userId,offset, limit, startDate, endDate);
    List<ScoreEntryDTO> dtos = new ArrayList<ScoreEntryDTO>();
    for (ScoreEntryEntity entity : entities) {
      if(entity.getEnabled()==null) entity.setEnabled(true);
      dtos.add(convert(entity));
    }
    return dtos;
  }

  public List<Object[]>  countGroupdByScores(long typeId, long startDate, long endDate) {

    return scoreEntryDAO.countEnabledGroupdByScores(typeId, startDate, endDate);

  }




  public long getPromotersCount(long typeId, boolean enabled, long startDate, long endDate) {

    return scoreEntryDAO.getPromotersCount(typeId, enabled, startDate, endDate);
  }


  public long getDetractorsCount(long typeId, boolean enabled, long startDate, long endDate) {

    return scoreEntryDAO.getDetractorsCount(typeId, enabled, startDate, endDate);
  }



  public long getScoreCount(long typeId, boolean enabled, long startDate, long endDate) {
    return scoreEntryDAO.getScoreEntriesCount(typeId, enabled, startDate, endDate);
  }


  public long getPromotersCountByDate(long typeId, long toDate) {

    return scoreEntryDAO.getEnabledPromotersCountByDate(typeId, toDate);
  }


  public long getDetractorsCountByDate(long typeId, long toDate) {

    return scoreEntryDAO.getEnabledDetractorsCountByDate(typeId, toDate);
  }



  public long getScoreCountByDate(long typeId, long toDate) {
    return scoreEntryDAO.getEnabledScoreEntriesCountByDate(typeId, toDate);
  }

  public long getPromotersCountByPeriod(long typeId, long fromDate, long toDate) {

    return scoreEntryDAO.getEnabledPromotersCountByPeriod(typeId,fromDate , toDate);
  }


  public long getDetractorsCountByPeriod(long typeId, long fromDate, long toDate) {

    return scoreEntryDAO.getEnabledDetractorsCountByPeriod(typeId,fromDate , toDate);
  }



  public long getScoreCountByPeriod(long typeId, long fromDate, long toDate) {
    return scoreEntryDAO.getEnabledScoreEntriesCountByPeriod(typeId,fromDate , toDate);
  }

  public double getMeanScore(long typeId, long startDate, long endDate) {
    return scoreEntryDAO.getEnabledScoresAvg(typeId, startDate, endDate);
  }

  public long getAllCount(long typeId, long fromDate, long toDate) {
    return scoreEntryDAO.getAllCount(typeId,fromDate , toDate);
  }



  private ScoreEntryEntity convert(ScoreEntryDTO dto) {
    ScoreEntryEntity entity = new ScoreEntryEntity();
    entity.setId(dto.getId());
    entity.setUserId(dto.getUserId());
    entity.setScore(dto.getScore());
    entity.setPostedTime(dto.getPostedTime());
    entity.setComment(dto.getComment());
    entity.setLastAppereance(dto.getLastAppereance());
    entity.setEnabled(dto.getEnabled());
    entity.setTypeId(dto.getTypeId());
    entity.setResponded(dto.getResponded());

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
    dto.setEnabled(entity.getEnabled());
    dto.setTypeId(entity.getTypeId());
    dto.setResponded(entity.getResponded());

    return dto;
  }

}

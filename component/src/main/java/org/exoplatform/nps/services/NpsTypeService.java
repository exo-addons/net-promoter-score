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

import org.exoplatform.nps.dao.ScoreTypeDAO;
import org.exoplatform.nps.dto.ScoreTypeDTO;
import org.exoplatform.nps.entity.ScoreTypeEntity;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by The eXo Platform SAS
 */
public class NpsTypeService {
  private  final Log LOG = ExoLogger.getLogger(NpsTypeService.class);


  private ScoreTypeDAO scoreTypeDAO;


  public NpsTypeService() {

    this.scoreTypeDAO = new ScoreTypeDAO();
  }

  public Boolean save(ScoreTypeDTO entity, boolean newType) {
    try {
      if (entity == null) {
        throw new IllegalStateException("Parameter 'entity' is null");
      }

      if (newType) {
         scoreTypeDAO.create(convert(entity));
       } else {
         scoreTypeDAO.update(convert(entity));
       }
    } catch (IllegalStateException e) {
      LOG.error("Cannot save the score", e.getMessage());
      return false;
    }
    return true;
  }

  public void remove(ScoreTypeDTO entity) {
   if (entity == null) {
      throw new IllegalStateException("Parameter 'entity' = + "+entity+ " or 'entity.id' is null");
    }
    scoreTypeDAO.delete(convert(entity));
  }

  public List<ScoreTypeDTO> getScoreTypes(int offset, int limit) {
    if (offset < 0) {
      throw new IllegalArgumentException("Method getScores - Parameter 'offset' must be positive");
    }
    List<ScoreTypeEntity> entities = scoreTypeDAO.getScoreTypes(offset, limit);
    List<ScoreTypeDTO> dtos = new ArrayList<ScoreTypeDTO>();
    for (ScoreTypeEntity entity : entities) {
      dtos.add(convert(entity));
    }
    return dtos;
  }



  public ScoreTypeDTO getScoreType(long id) {
    List<ScoreTypeEntity> entities=scoreTypeDAO.getScoreTypebyId(id);
    if (entities.size()!=0){
      return convert(entities.get(0));
    }
    return null;
  }



  public long getScoreTypeCount() {
    return scoreTypeDAO.getScoreTypesCount();
  }



  private ScoreTypeEntity convert(ScoreTypeDTO dto) {
    ScoreTypeEntity entity = new ScoreTypeEntity();
    entity.setId(dto.getId());
    entity.setTypeName(dto.getTypeName());
    entity.setQuestion(dto.getQuestion());
    entity.setIsDefault(dto.getIsDefault());
    entity.setFollowUpPassive(dto.getFollowUpPassive());
    entity.setFollowUpPromoter(dto.getFollowUpPromoter());
    entity.setFollowUpDetractor(dto.getFollowUpDetractor());
    entity.setAnonymous(dto.getAnonymous());
    return entity;
  }

  private ScoreTypeDTO convert(ScoreTypeEntity entity) {
    ScoreTypeDTO dto = new ScoreTypeDTO();
    dto.setId(entity.getId());
    dto.setTypeName(entity.getTypeName());
    dto.setQuestion(entity.getQuestion());
    dto.setIsDefault(entity.getIsDefault());
    dto.setFollowUpPassive(entity.getFollowUpPassive());
    dto.setFollowUpPromoter(entity.getFollowUpPromoter());
    dto.setFollowUpDetractor(entity.getFollowUpDetractor());
    dto.setAnonymous(entity.getAnonymous());
    return dto;
  }


}

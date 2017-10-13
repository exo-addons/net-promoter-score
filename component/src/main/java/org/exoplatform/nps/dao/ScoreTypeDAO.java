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
package org.exoplatform.nps.dao;

import org.exoplatform.commons.persistence.impl.GenericDAOJPAImpl;
import org.exoplatform.nps.entity.ScoreTypeEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by The eXo Platform SAS
 */
public class ScoreTypeDAO extends GenericDAOJPAImpl<ScoreTypeEntity, String> {
    private static final Logger LOG = LoggerFactory.getLogger(ScoreTypeDAO.class);

    public List<ScoreTypeEntity> getScoreTypes(int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreTypeEntity.findAllOrderBy", ScoreTypeEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return findAll();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get score Types with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public  long getScoreTypesCount() {
        try {
                return getEntityManager().createNamedQuery("scoreTypeEntity.count", Long.class).getSingleResult();

        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scoreTypes count.", e);
            throw e;
        }
    }


    public List<ScoreTypeEntity> getScoreTypebyId(long id) {
        try {
            return getEntityManager().createNamedQuery("scoreTypeEntity.findById", ScoreTypeEntity.class)
                    .setParameter("id", id)
                    .getResultList();
        }  catch (Exception e) {
            LOG.warn("Exception while attempting to get request", e);
            throw e;
        }
    }

}
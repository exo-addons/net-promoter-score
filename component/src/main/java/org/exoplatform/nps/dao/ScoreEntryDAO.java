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
import org.exoplatform.nps.entity.ScoreEntryEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SAS
 */
public class ScoreEntryDAO extends GenericDAOJPAImpl<ScoreEntryEntity, String> {
    private static final Logger LOG = LoggerFactory.getLogger(ScoreEntryDAO.class);

    public List<ScoreEntryEntity> getScoreEntries(int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findAllOrderBy", ScoreEntryEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .getResultList();
            } else {
                return findAll();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public  long getScoreEntriesCount() {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.count", Long.class).getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }

    public  long getPromotersCount() {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countPromoters", Long.class).getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }

    public  long getDetractorsCount() {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countDetractors", Long.class).getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }



    public List<ScoreEntryEntity> getScoreEntriesByUserId(String userId, int offset, int limit) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findByUserId", ScoreEntryEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("userId", userId)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findByUserId", ScoreEntryEntity.class)
                        .setParameter("userId", userId)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }




    public List<ScoreEntryEntity> getScoreEntrybyId(long id) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.findById", ScoreEntryEntity.class)
                    .setParameter("id", id)
                    .getResultList();
        }  catch (Exception e) {
            LOG.warn("Exception while attempting to get request", e);
            throw e;
        }
    }

}
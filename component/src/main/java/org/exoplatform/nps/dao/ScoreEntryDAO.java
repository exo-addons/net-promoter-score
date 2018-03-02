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

    public List<ScoreEntryEntity> getScoreEntries(long typeId, int offset, int limit, long startDate, long endDate) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findAllOrderByDesc", ScoreEntryEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findAllOrderByDesc", ScoreEntryEntity.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public List<ScoreEntryEntity> getPassiveScoreEntries(long typeId, int offset, int limit, long startDate, long endDate) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findPassives", ScoreEntryEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findPassives", ScoreEntryEntity.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public List<ScoreEntryEntity> getPromoterScoreEntries(long typeId, int offset, int limit, long startDate, long endDate) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findPromoters", ScoreEntryEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findPromoters", ScoreEntryEntity.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }

    public List<ScoreEntryEntity> getDetractorScoreEntries(long typeId, int offset, int limit, long startDate, long endDate) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findDetractors", ScoreEntryEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findDetractors", ScoreEntryEntity.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            }
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores with offset = '" + offset + "' and limit = '" + limit + "'.", e);
            throw e;
        }
    }


    public List<ScoreEntryEntity> getFirstScoreEntries(long typeId) {
        try {

                return getEntityManager().createNamedQuery("scoreEntryEntity.findAllOrderByAsc", ScoreEntryEntity.class)
                        .setParameter("typeId", typeId)
                        .setFirstResult(0)
                        .setMaxResults(1)
                        .getResultList();

        } catch (Exception e) {
            LOG.warn("Exception while attempting to get first scores", e);
            throw e;
        }
    }

    public  long getScoreEntriesCount(long typeId,boolean enabled, long startDate, long endDate) {
        try {
            if(enabled==true){
                return getEntityManager().createNamedQuery("scoreEntryEntity.countEnabled", Long.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getSingleResult();
            }else{
                return getEntityManager().createNamedQuery("scoreEntryEntity.count", Long.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getSingleResult();
            }

        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }

    public  long getPromotersCount(long typeId,boolean enabled, long startDate, long endDate) {
        try {

            if(enabled==true){
                return getEntityManager().createNamedQuery("scoreEntryEntity.countPromoters", Long.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getSingleResult();
            }else{
                return getEntityManager().createNamedQuery("scoreEntryEntity.countAllPromoters", Long.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getSingleResult();
            }

        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }



    }

    public  long getDetractorsCount(long typeId,boolean enabled, long startDate, long endDate) {
        try {
            if(enabled==true){
                return getEntityManager().createNamedQuery("scoreEntryEntity.countDetractors", Long.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getSingleResult();
            }else{
                return getEntityManager().createNamedQuery("scoreEntryEntity.countAllDetractors", Long.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getSingleResult();
            }

        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }



    public  long getScoreEntriesCountByDate(long typeId,long toDate) {
        try {

                return getEntityManager().createNamedQuery("scoreEntryEntity.countEnabledByDate", Long.class)
                        .setParameter("typeId", typeId)
                        .setParameter("toDate", toDate)
                        .getSingleResult();


        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }

    public  long getPromotersCountByDate(long typeId, long toDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countPromotersByDate", Long.class)
                    .setParameter("typeId", typeId)
                    .setParameter("toDate", toDate)
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }

    public  long getDetractorsCountByDate(long typeId, long toDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countDetractorsByDate", Long.class)
                    .setParameter("typeId", typeId)
                    .setParameter("toDate", toDate)
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }



    public  long getScoreEntriesCountByPeriod(long typeId, long fromDate, long toDate) {
        try {

            return getEntityManager().createNamedQuery("scoreEntryEntity.countEnabledByPeriod", Long.class)
                    .setParameter("typeId", typeId)
                    .setParameter("fromDate", fromDate)
                    .setParameter("toDate", toDate)
                    .getSingleResult();


        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }

    public  long getPromotersCountByPeriod(long typeId, long fromDate, long toDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countPromotersByPeriod", Long.class)
                    .setParameter("typeId", typeId)
                    .setParameter("fromDate", fromDate)
                    .setParameter("toDate", toDate)
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }

    public  long getDetractorsCountByPeriod(long typeId, long fromDate, long toDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countDetractorsByPeriod", Long.class)
                    .setParameter("typeId", typeId)
                    .setParameter("fromDate", fromDate)
                    .setParameter("toDate", toDate)
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }


    public List<ScoreEntryEntity> getScoreEntriesByUserId(long typeId,String userId, int offset, int limit, long startDate, long endDate) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findByUserId", ScoreEntryEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("userId", userId)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findByUserId", ScoreEntryEntity.class)
                        .setParameter("userId", userId)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
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

    public List<Object[]> countGroupdByScores(long typeId, Long startDate, Long endDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countGroupdByScores", Object[].class)
                    .setParameter("typeId", typeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        }  catch (Exception e) {
            LOG.warn("Exception while attempting to get request", e);
            throw e;
        }
    }


    public double getScoresAvg(long typeId, long startDate, long endDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.avgEnabled", Double.class)
                    .setParameter("typeId", typeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getSingleResult();
        }  catch (NullPointerException e) {
            return 0;
        }   catch (Exception e) {
            LOG.warn("Exception while attempting to get request", e);
            throw e;
        }
    }


}
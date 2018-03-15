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

    public List<ScoreEntryEntity> getAllScoreEntries(long typeId, int offset, int limit, long startDate, long endDate) {
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

    public List<ScoreEntryEntity> getAllPassiveScoreEntries(long typeId, int offset, int limit, long startDate, long endDate) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findAllPassives", ScoreEntryEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findAllPassives", ScoreEntryEntity.class)
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

    public List<ScoreEntryEntity> getAllPromoterScoreEntries(long typeId, int offset, int limit, long startDate, long endDate) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findAllPromoters", ScoreEntryEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findAllPromoters", ScoreEntryEntity.class)
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

    public List<ScoreEntryEntity> getAllDetractorScoreEntries(long typeId, int offset, int limit, long startDate, long endDate) {
        try {
            if (offset >= 0 && limit > 0) {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findAllDetractors", ScoreEntryEntity.class)
                        .setFirstResult(offset)
                        .setMaxResults(limit)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getResultList();
            } else {
                return getEntityManager().createNamedQuery("scoreEntryEntity.findAllDetractors", ScoreEntryEntity.class)
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
                return getEntityManager().createNamedQuery("scoreEntryEntity.countAllresponded", Long.class)
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
                return getEntityManager().createNamedQuery("scoreEntryEntity.countEnabledPromoters", Long.class)
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
                return getEntityManager().createNamedQuery("scoreEntryEntity.countEnabledDetractors", Long.class)
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



    public  long getEnabledScoreEntriesCountByDate(long typeId,long toDate) {
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

    public  long getEnabledPromotersCountByDate(long typeId, long toDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countEnabledPromotersByDate", Long.class)
                    .setParameter("typeId", typeId)
                    .setParameter("toDate", toDate)
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }

    public  long getEnabledDetractorsCountByDate(long typeId, long toDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countEnabledDetractorsByDate", Long.class)
                    .setParameter("typeId", typeId)
                    .setParameter("toDate", toDate)
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }



    public  long getEnabledScoreEntriesCountByPeriod(long typeId, long fromDate, long toDate) {
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

    public  long getEnabledPromotersCountByPeriod(long typeId, long fromDate, long toDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countEnabledPromotersByPeriod", Long.class)
                    .setParameter("typeId", typeId)
                    .setParameter("fromDate", fromDate)
                    .setParameter("toDate", toDate)
                    .getSingleResult();
        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }

    public  long getEnabledDetractorsCountByPeriod(long typeId, long fromDate, long toDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countEnabledDetractorsByPeriod", Long.class)
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

    public List<Object[]> countEnabledGroupdByScores(long typeId, Long startDate, Long endDate) {
        try {
            return getEntityManager().createNamedQuery("scoreEntryEntity.countEnabledGroupdByScores", Object[].class)
                    .setParameter("typeId", typeId)
                    .setParameter("startDate", startDate)
                    .setParameter("endDate", endDate)
                    .getResultList();
        }  catch (Exception e) {
            LOG.warn("Exception while attempting to get request", e);
            throw e;
        }
    }


    public double getEnabledScoresAvg(long typeId, long startDate, long endDate) {
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



    public  long getAllCount(long typeId, long startDate, long endDate) {
        try {
                return getEntityManager().createNamedQuery("scoreEntryEntity.countAll", Long.class)
                        .setParameter("typeId", typeId)
                        .setParameter("startDate", startDate)
                        .setParameter("endDate", endDate)
                        .getSingleResult();

        } catch (Exception e) {
            LOG.warn("Exception while attempting to get scores count.", e);
            throw e;
        }
    }


}
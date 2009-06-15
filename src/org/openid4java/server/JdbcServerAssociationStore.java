/*
 * Copyright 2006-2007 Sxip Identity Corporation
 */

package org.openid4java.server;

import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;

import java.util.*;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JDBC implementation for the ServerAssociationStore interface.
 * <p>
 * The JdbcServerAssociation store requires a javax.sql.DataSource to be
 * configured and passed in to it with the setDataSource setter method.
 * The table name also needs to be specified, either through the constructor,
 * or through the setTableName setter.
 * <p>
 * The specified table must have the following structure:
 * <ul>
 * <li>handle : string : primary key</li>
 * <li>type : string</li>
 * <li>mackey : string</li>
 * <li>expdate : date</li>
 * </ul>
 *
 * @author Marius Scurtescu, Johnny Bufu
 */
public class JdbcServerAssociationStore extends JdbcDaoSupport
        implements ServerAssociationStore
{
    private static Log _log = LogFactory.getLog(JdbcServerAssociationStore.class);
    private static final boolean DEBUG = _log.isDebugEnabled();

    private static Random _random = new Random(System.currentTimeMillis());

    private String _tableName;

    // todo: removeExpired();

    public JdbcServerAssociationStore()
    {
    }

    public JdbcServerAssociationStore(String tableName)
    {
        _tableName = tableName;
    }

    public String getTableName()
    {
        return _tableName;
    }

    public void setTableName(String tableName)
    {
        this._tableName = tableName;
    }

    public Association generate(String type, int expiryIn)
            throws AssociationException
    {
        String sql = "INSERT INTO " + _tableName +
                " (handle, type, mackey, expdate) VALUES (?,?,?,?)";

        JdbcTemplate jdbcTemplate = getJdbcTemplate();

        int attemptsLeft = 5;

        while (attemptsLeft > 0)
        {
            try
            {
                String handle = Long.toHexString(_random.nextLong());

                Association association =
                        Association.generate(type, handle, expiryIn);

                int cnt = jdbcTemplate.update(sql,
                        new Object[] {
                                association.getHandle(),
                                association.getType(),
                                new String(Base64.encodeBase64(
                                        association.getMacKey().getEncoded())),
                                association.getExpiry()
                        });

                if (cnt == 1)
                {
                    if (DEBUG)
                        _log.debug("Generated association, handle: " + handle +
                                   " type: " + type +
                                   " expires in: " + expiryIn + " seconds.");

                    return association;
                }
            }
            catch (DataAccessException e)
            {
                _log.error("Error generating association; attempts left: "
                           + (attemptsLeft-1), e);
            }

            attemptsLeft--;
        }

        throw new AssociationException(
                "JDBCServerAssociationStore: Error generating association.");
    }

    public Association load(String handle)
    {
        try
        {
            String sql = "SELECT type,mackey,expdate FROM " + _tableName +
                    " WHERE handle=?";

            JdbcTemplate jdbcTemplate = getJdbcTemplate();

            Map res = jdbcTemplate.queryForMap(sql, new Object[] {handle});

            String type = (String) res.get("type");
            String macKey = (String) res.get("mackey");
            Date expDate = (Date) res.get("expdate");

            if (type == null || macKey == null || expDate == null)
                throw new AssociationException("Invalid association data " +
                        "retrived from database; cannot create Association " +
                        "object for handle: " + handle);

            Association assoc;

            if (Association.TYPE_HMAC_SHA1.equals(type))
                assoc = Association.createHmacSha1(handle,
                        Base64.decodeBase64(macKey.getBytes() ), expDate);

            else if (Association.TYPE_HMAC_SHA256.equals(type))
                assoc = Association.createHmacSha256(handle,
                        Base64.decodeBase64(macKey.getBytes() ), expDate);

            else
                throw new AssociationException("Invalid association type " +
                        "retrieved from database: " + type);

            if (DEBUG)
                _log.debug("Retrieved association from database, handle: " + handle);

            return assoc;
        }
        catch (AssociationException ase )
        {
            _log.error("Error rerieving association from database.", ase);
            return null;
        }
        catch (IncorrectResultSizeDataAccessException rse)
        {
            _log.warn("Association not found in the database for handle: " + handle);
            return null;
        }
        catch (DataAccessException dae)
        {
            _log.error("Error retrieving association from database, handle: "
                       + handle, dae);
            return null;
        }
    }

    public void remove(String handle)
    {
        try
        {
            String sql = "DELETE FROM " + _tableName + " WHERE handle=?";

            JdbcTemplate jdbcTemplate = getJdbcTemplate();

            int cnt = jdbcTemplate.update(sql, new Object[] { handle } );

            if (cnt == 1 && DEBUG)
                _log.debug("Removed association, handle: " + handle);

            if (cnt != 1)
                _log.warn("Trying to remove handle: " + handle +
                          " from database; affected entries: " + cnt);
        }
        catch (Exception e)
        {
            _log.error("Error removing association from database.", e);
        }
    }
}

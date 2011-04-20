
package org.openid4java.consumer ;

import java.util.Date;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.association.Association;
import org.openid4java.association.AssociationException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

/**
 * 
 * The specified table must have the following structure:
 * <ul>
 * <li>opurl : string </li>
 * <li>handle : string : primary key</li>
 * <li>type : string</li>
 * <li>mackey : string</li>
 * <li>expdate : date</li>
 * </ul>
 * 
 * @author Andrew Evenson, Graff Haley
 * @created May 19, 2008
 */
public class JdbcConsumerAssociationStore
		extends JdbcDaoSupport
		implements ConsumerAssociationStore
{
	private static Log _log = LogFactory.getLog ( JdbcConsumerAssociationStore.class ) ;

	private String _tableName ;
	private String _sqlInsert ;
	private String _sqlDelete ;
	private String _sqlCleanup ;
	private String _sqlSelect ;
	private String _sqlSelectAlt ;


	public JdbcConsumerAssociationStore ( )
	{
	}

	public JdbcConsumerAssociationStore ( String tableName )
	{
		setTableName ( tableName ) ;
	}

	public String getTableName ( )
	{
		return _tableName ;
	}

	public void setTableName ( String tableName )
	{
		this._tableName = tableName ;
		this._sqlInsert = "INSERT INTO " + _tableName + " VALUES (?,?,?,?,?)" ;
		this._sqlDelete = "DELETE FROM " + _tableName
							+ " WHERE opurl=? AND handle=?" ;
		this._sqlCleanup = "DELETE FROM " + _tableName + " WHERE expdate < ?" ;
		this._sqlSelect = "SELECT * FROM " + _tableName
							+ " WHERE opurl=? AND handle=?" ;
		this._sqlSelectAlt = "SELECT * FROM " + _tableName
								+ " T1 JOIN (SELECT opurl, max(expdate) AS expdate FROM " + _tableName
								+ " WHERE opurl=? GROUP BY opurl) T2 ON (T1.expdate = T2.expdate AND T1.opurl = T2.opurl)" ;
	}

	public Association load ( String opUrl, String handle )
	{		
		try
		{
			JdbcTemplate jdbcTemplate = getJdbcTemplate ( ) ;

			Map res = jdbcTemplate.queryForMap ( _sqlSelect, new Object[]
				{ opUrl, handle } ) ;

			String type = (String) res.get ( "type" ) ;
			String macKey = (String) res.get ( "mackey" ) ;
			Date expDate = (Date) res.get ( "expdate" ) ;

			if ( type == null || macKey == null || expDate == null )
				throw new AssociationException (
													"Invalid association data retrived from database; cannot create Association "
															+ "object for handle: "
															+ handle ) ;

			Association assoc ;

			if ( Association.TYPE_HMAC_SHA1.equals ( type ) )
				assoc = Association.createHmacSha1 (	handle,
														Base64.decodeBase64 ( macKey.getBytes ( ) ),
														expDate ) ;

			else if ( Association.TYPE_HMAC_SHA256.equals ( type ) )
				assoc = Association.createHmacSha256 (	handle,
														Base64.decodeBase64 ( macKey.getBytes ( ) ),
														expDate ) ;

			else
				throw new AssociationException (
													"Invalid association type "
															+ "retrieved from database: "
															+ type ) ;

			if ( _log.isDebugEnabled ( ) )
				_log.debug ( "Retrieved association for handle: " + handle
								+ " from table: " + _tableName ) ;

			return assoc ;
		}
		catch ( AssociationException ase )
		{
			_log.error ( "Error retrieving association from table: "
							+ _tableName, ase ) ;
			return null ;
		}
		catch ( IncorrectResultSizeDataAccessException rse )
		{
			_log.warn ( "Association not found for handle: " + handle
						+ " in the table: " + _tableName ) ;
			return null ;
		}
		catch ( DataAccessException dae )
		{
			_log.error ( "Error retrieving association for handle: " + handle
							+ "from table: " + _tableName, dae ) ;
			return null ;
		}
	}

	public Association load ( String opUrl )
	{		
		try
		{
			JdbcTemplate jdbcTemplate = getJdbcTemplate ( ) ;

			Map res = jdbcTemplate.queryForMap ( _sqlSelectAlt, new Object[]
				{ opUrl } ) ;

			String handle = (String) res.get ( "handle" ) ;
			String type = (String) res.get ( "type" ) ;
			String macKey = (String) res.get ( "mackey" ) ;
			Date expDate = (Date) res.get ( "expdate" ) ;

			Association assoc ;

            if ( expDate == null || ( type == null || macKey == null ) &&
                 ! Association.FAILED_ASSOC_HANDLE.equals(handle) ) {
				throw new AssociationException (
													"Invalid expiry date retrived from database; cannot create Association "
															+ "object for handle: "
															+ handle ) ;

            } else if (Association.FAILED_ASSOC_HANDLE.equals(handle)) {
                assoc = Association.getFailedAssociation(expDate);

            } else if ( Association.TYPE_HMAC_SHA1.equals ( type ) ) {
				assoc = Association.createHmacSha1 (	handle,
														Base64.decodeBase64 ( macKey.getBytes ( ) ),
														expDate ) ;

            } else if ( Association.TYPE_HMAC_SHA256.equals ( type ) ) {
				assoc = Association.createHmacSha256 (	handle,
														Base64.decodeBase64 ( macKey.getBytes ( ) ),
														expDate ) ;

            } else {
				throw new AssociationException (
													"Invalid association type "
															+ "retrieved from database: "
															+ type ) ;

            }

			if ( _log.isDebugEnabled ( ) )
				_log.debug ( "Retrieved association for handle: " + handle
								+ " from table: " + _tableName ) ;

			return assoc ;
		}
		catch ( AssociationException ase )
		{
			_log.error ( "Error retrieving association from table: "
							+ _tableName, ase ) ;
			return null ;
		}
		catch ( IncorrectResultSizeDataAccessException rse )
		{
			_log.warn ( "Association not found for opUrl: " + opUrl
						+ " in the table: " + _tableName ) ;
			return null ;
		}
		catch ( DataAccessException dae )
		{
			_log.error ( "Error retrieving association for opUrl: " + opUrl
							+ "from table: " + _tableName, dae ) ;
			return null ;
		}
	}

	public void remove ( String opUrl, String handle )
	{		
		try
		{
			JdbcTemplate jdbcTemplate = getJdbcTemplate ( ) ;

			int cnt = jdbcTemplate.update ( _sqlDelete, new Object[]
				{ opUrl, handle } ) ;
		}
		catch ( Exception e )
		{
			_log.error (	"Error removing association from table: "
									+ _tableName,
							e ) ;
		}
	}

	public void save ( String opUrl, Association association )
	{
		cleanupExpired ( ) ;
		
		try
		{
			JdbcTemplate jdbcTemplate = getJdbcTemplate ( ) ;

			int cnt = jdbcTemplate.update ( _sqlInsert,
											new Object[]
												{
												 	opUrl,
													association.getHandle ( ),
													association.getType ( ),
													association.getMacKey ( ) == null ? null :
													    new String (
																	Base64.encodeBase64 ( association.getMacKey ( ).getEncoded ( ) ) ),
													association.getExpiry ( ) } ) ;
		}
		catch ( Exception e )
		{
			_log.error ( "Error saving association to table: " + _tableName, e ) ;
		}
	}

	private void cleanupExpired ( )
	{
		try
		{
			Date boundary = new Date ( ) ;
			JdbcTemplate jdbcTemplate = getJdbcTemplate ( ) ;
			int cnt = jdbcTemplate.update ( _sqlCleanup, new Object[]
				{ boundary } ) ;

			if ( _log.isDebugEnabled ( ) )
				_log.debug ( "Client associations cleanup removed " + cnt
								+ " entries" ) ;
		}
		catch ( Exception e )
		{
			_log.error ( "Error cleaning up client associations from table: "
							+ _tableName, e ) ;
		}
	}

}

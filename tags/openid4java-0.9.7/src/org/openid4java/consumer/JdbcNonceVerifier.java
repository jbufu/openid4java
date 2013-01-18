
package org.openid4java.consumer ;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;

import java.util.Date;

/**
 * 
 * JDBC implementation of a NonceVerifier.
 * <p>
 * The JdbcNonceVerifier requires a a javax.sql.DataSource to be configured
 * and passed in to it with the setDataSource setter method. The table name
 * also needs to be specified, either through the constructor, or through the
 * setTableName setter.
 * </p>
 * <p>
 * Since the nonces are constructed on the web server and not on the shared
 * database server, they may accidentally collide. Also, if the machines
 * clocks are out of sync, the nonces from the machine that is behind may be
 * removed prematurely from the database by the other machine.
 * </p>
 * <p>
 * The specified table must have the following structure:
 * <ul>
 * <li>opurl : string</li>
 * <li>nonce : string</li>
 * <li>date : datetime</li>
 * <li>primary key : opurl, nonce</li>
 * </ul>
 * </p>
 * 
 * @author Andrew Evenson, Graff Haley
 * @created May 19, 2008
 */
public class JdbcNonceVerifier
		extends JdbcDaoSupport
		implements NonceVerifier
{
	private static Log _log = LogFactory.getLog ( JdbcNonceVerifier.class ) ;

	private NonceVerifier _verifier ;
	private String _tableName ;
	private String _deleteSQL ;
	private String _insertSQL ;


	public JdbcNonceVerifier ( int maxAge )
	{
		_verifier = new GenericNonceVerifier ( maxAge ) ;
	}
	
	public JdbcNonceVerifier ( int maxAge, String tableName )
	{
		this ( maxAge ) ;
		setTableName ( tableName ) ;
	}

	public int getMaxAge ( )
	{
		return _verifier.getMaxAge ( ) ;
	}

    public void setMaxAge(int ageSeconds)
    {
        _verifier.setMaxAge(ageSeconds);
    }

    public int seen ( String opUrl, String nonce )
	{
		return _verifier.seen ( opUrl, nonce ) ;
	}

	public String getTableName ( )
	{
		return _tableName ;
	}

	public void setTableName ( String tableName )
	{
		this._tableName = tableName ;
		this._deleteSQL = "DELETE FROM " + tableName + " WHERE date<?" ;
		this._insertSQL = "INSERT INTO " + tableName + " (opurl, nonce, date) VALUES (?,?,?)" ;
	}


	private class GenericNonceVerifier
			extends AbstractNonceVerifier
	{
		public GenericNonceVerifier ( int maxAge )
		{
			super ( maxAge ) ;
		}


		/**
		 * 
		 * Implementation of the abstract nonce verifier. Uses the primary key
		 * integrity constraint to evaluate nonces. This prevents a gap
		 * between check and insert. Also, triggers the cleanup of old nonces.
		 * 
		 * @param now
		 * @param opUrl
		 * @param nonce
		 * @return
		 */
		protected int seen ( Date now, String opUrl, String nonce )
		{
			cleanupAged ( ) ;
			JdbcTemplate jdbcTemplate = getJdbcTemplate ( ) ;

			try
			{
				jdbcTemplate.update ( _insertSQL, new Object[]
					{ opUrl, nonce, now } ) ;
				return OK ;
			}
			catch ( DataIntegrityViolationException e )
			{
				_log.warn ( "Nonce already seen. Possible replay attack!" ) ;
			}
			catch ( Exception e )
			{
				_log.error ( "Problem executing database method", e ) ;
			}

			return SEEN ;
		}

		private void cleanupAged ( )
		{
			try
			{
				Date boundary = new Date ( System.currentTimeMillis ( ) - 1000L * _maxAgeSeconds ) ;
				JdbcTemplate jdbcTemplate = getJdbcTemplate ( ) ;
				int cnt = jdbcTemplate.update ( _deleteSQL, new Object[]
					{ boundary } ) ;

				if ( _log.isDebugEnabled ( ) ) _log.debug ( "Client nonce cleanup removed " + cnt + " entries" ) ;
			}
			catch ( Exception e )
			{
				_log.error ( "Error cleaning up client nonces from table: " + _tableName, e ) ;
			}
		}
	}
}

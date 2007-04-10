/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 * [See end of file]
 */

package com.hp.hpl.jena.sdb.layout2.hash;

import static com.hp.hpl.jena.sdb.sql.SQLUtils.sqlStr;

import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.hp.hpl.jena.sdb.SDBException;
import com.hp.hpl.jena.sdb.layout2.FmtLayout2;
import com.hp.hpl.jena.sdb.layout2.TableNodes;
import com.hp.hpl.jena.sdb.layout2.TablePrefixes;
import com.hp.hpl.jena.sdb.layout2.TableTriples;
import com.hp.hpl.jena.sdb.sql.MySQLEngineType;
import com.hp.hpl.jena.sdb.sql.SDBConnection;
import com.hp.hpl.jena.sdb.sql.SDBExceptionSQL;


public class FmtLayout2HashMySQL extends FmtLayout2
{
    static private Log log = LogFactory.getLog(FmtLayout2HashMySQL.class) ;
    private MySQLEngineType engineType ;
    
    public FmtLayout2HashMySQL(SDBConnection connection, MySQLEngineType tableType)
    { 
        super(connection) ;
        engineType = tableType ;
        if ( engineType == null )
        {
            log.fatal("Engine type is null") ;
            throw new SDBException("Engine type is null") ;
        }
    }

    @Override
    protected void formatTableTriples()
    {
        dropTable(TableTriples.tableName) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE TABLE "+TableTriples.tableName+" (",
                                 "    s BIGINT  NOT NULL ,",
                                 "    p BIGINT  NOT NULL ,",
                                 "    o BIGINT  NOT NULL ,",
                                 "    PRIMARY KEY (s, p, o)",
                                 ") ENGINE="+engineType.getEngineName()                
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableTriples.tableName+"'",ex) ;
        }
    }

    @Override
    protected void dropIndexesTableTriples()
    {
        try {
            connection().exec("DROP INDEX PredObj ON "+TableTriples.tableName) ;
            connection().exec("DROP INDEX ObjSubj ON "+TableTriples.tableName) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException dropping indexes '"+TableTriples.tableName+"'",ex) ; }
    }
    
    @Override
    protected void formatTableNodes()
    {
        dropTable(TableNodes.tableName) ;
        try { 
            // MySQL: VARCHAR BINARY = VARCHAR COLLATE utf8_bin 
            connection().exec(sqlStr ("CREATE TABLE "+TableNodes.tableName+" (",
                                 "   hash BIGINT NOT NULL DEFAULT 0,",
                                 "   lex TEXT BINARY CHARACTER SET utf8 ,",
                                 "   lang VARCHAR(10) BINARY CHARACTER SET utf8 NOT NULL default '',",
                                 "   datatype VARCHAR("+TableNodes.DatatypeUriLength+") BINARY CHARACTER SET utf8 NOT NULL default '',",
                                 "   type int unsigned NOT NULL default '0',",
                                 "   PRIMARY KEY Hash  (hash)",
                                 ") ENGINE="+engineType.getEngineName()+" DEFAULT CHARSET=utf8;"  
                    )) ;
            connection().exec("CREATE UNIQUE INDEX Hash ON "+TableNodes.tableName+" (hash)") ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException formatting table '"+TableNodes.tableName+"'",ex) ;
        }
    }

    @Override
    protected void formatTablePrefixes()
    {
        dropTable(TablePrefixes.tableName) ;
        try { 
            connection().exec(sqlStr(
                                 "CREATE TABLE "+TablePrefixes.tableName+" (",
                                 "    prefix VARCHAR("+TablePrefixes.prefixColWidth+") BINARY NOT NULL ,",
                                 "    uri VARCHAR("+TablePrefixes.uriColWidth+") BINARY NOT NULL ,", 
                                 "    PRIMARY KEY  (prefix)",
                                 ") ENGINE="+engineType.getEngineName()+" DEFAULT CHARSET=utf8"            
                    )) ;
        } catch (SQLException ex)
        {
            throw new SDBExceptionSQL("SQLException resetting table '"+TablePrefixes.tableName+"'",ex) ;
        }
    }
    
    @Override
    protected void truncateTable(String tableName)
    { 
        try { 
            // MySQL note: DELETE FROM is transactional, TRUNCATE is not
            connection().exec("TRUNCATE "+tableName) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException : Can't truncate table: "+tableName, ex) ; }
    }
    
    @Override
    protected void dropTable(String tableName)
    {
        try { 
            connection().exec("DROP TABLE IF EXISTS "+tableName) ;
        } catch (SQLException ex)
        { throw new SDBExceptionSQL("SQLException : Can't drop table: "+tableName, ex) ; }
    }

}

/*
 * (c) Copyright 2006, 2007 Hewlett-Packard Development Company, LP
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of the author may not be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
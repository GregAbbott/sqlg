package org.umlg.sqlgraph.sql.dialect;

import org.umlg.sqlgraph.structure.PropertyType;

/**
 * Date: 2014/07/16
 * Time: 3:09 PM
 */
public class HsqldbDialect implements SqlDialect {

    @Override
    public String getVarcharArray() {
        return "VARCHAR(255) ARRAY DEFAULT ARRAY[]";
    }

    @Override
    public String getPrimaryKeyType() {
        return "BIGINT";
    }

    @Override
    public String getAutoIncrementPrimaryKeyConstruct() {
        return "GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY";
    }

    @Override
    public String propertyTypeToSqlDefinition(PropertyType propertyType) {

        switch (propertyType) {
            case BOOLEAN:
                return "BOOLEAN" ;
            case BYTE:
                return "TINYINT" ;
            case SHORT:
                return "TINYINT" ;
            case INTEGER:
                return "INTEGER" ;
            case LONG:
                return "BIGINT" ;
            case FLOAT:
                return "FLOAT" ;
            case DOUBLE:
                return "DOUBLE" ;
            case STRING:
                return "LONGVARCHAR";
        }

        return null;
    }

    @Override
    public String getForeignKeyTypeDefinition() {
        return "BIGINT NOT NULL";
    }
}

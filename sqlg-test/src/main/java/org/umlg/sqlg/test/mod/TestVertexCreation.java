package org.umlg.sqlg.test.mod;

import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Vertex;
import org.junit.Assume;
import org.junit.Test;
import org.umlg.sqlg.structure.SchemaManager;
import org.umlg.sqlg.structure.SqlgDataSource;
import org.umlg.sqlg.test.BaseTest;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Date: 2014/07/12
 * Time: 2:26 PM
 */
public class TestVertexCreation extends BaseTest {

    @Test
    public void testCreateEmptyVertex() throws SQLException {
        sqlG.addVertex();
        sqlG.tx().commit();
        try (Connection conn = SqlgDataSource.INSTANCE.get(this.sqlG.getJdbcUrl()).getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                StringBuilder sql = new StringBuilder("SELECT * FROM ");
                sql.append(this.sqlG.getSqlDialect().maybeWrapInQoutes(this.sqlG.getSqlDialect().getPublicSchema()));
                sql.append(".");
                sql.append(this.sqlG.getSqlDialect().maybeWrapInQoutes(SchemaManager.VERTEX_PREFIX + "vertex"));
                if (this.sqlG.getSqlDialect().needsSemicolon()) {
                    sql.append(";");
                }
                ResultSet rs = stmt.executeQuery(sql.toString());
                int countRows = 0;
                while (rs.next()) {
                    countRows++;
                }
                assertEquals(1, countRows);
                rs.close();
            }
        }
    }

    @Test
    public void testCreateVertexWithProperties() throws SQLException {
        Assume.assumeTrue(this.sqlG.getSqlDialect().supportsFloatValues());
        sqlG.addVertex(Element.LABEL, "Person",
                "boolean1", true,
                "short1", (short) 1,
                "integer1", 1,
                "long1", 1L,
                "float1", 1F,
                "double1", 1D,
                "name", "marko"
        );
        sqlG.addVertex(Element.LABEL, "Person",
                "boolean1", true,
                "short1", (short) 1,
                "integer1", 1,
                "long1", 1L,
                "float1", 1F,
                "double1", 1D,
                "name", "marko"
        );
        sqlG.tx().commit();

        try (Connection conn = SqlgDataSource.INSTANCE.get(this.sqlG.getJdbcUrl()).getConnection()) {
            try (Statement stmt = conn.createStatement()) {
                StringBuilder sql = new StringBuilder("SELECT * FROM ");
                sql.append(this.sqlG.getSqlDialect().maybeWrapInQoutes(this.sqlG.getSqlDialect().getPublicSchema()));
                sql.append(".");
                sql.append(this.sqlG.getSqlDialect().maybeWrapInQoutes(SchemaManager.VERTEX_PREFIX + "Person"));
                if (this.sqlG.getSqlDialect().needsSemicolon()) {
                    sql.append(";");
                }
                ResultSet rs = stmt.executeQuery(sql.toString());
                int countRows = 0;
                boolean boolean1 = false;
                short short1 = (short) -1;
                int integer1 = -1;
                long long1 = -1L;
                float float1 = -1F;
                double double1 = -1D;
                String name = "";
                while (rs.next()) {
                    boolean1 = rs.getBoolean("boolean1");
                    short1 = rs.getShort("short1");
                    integer1 = rs.getInt("integer1");
                    long1 = rs.getLong("long1");
                    float1 = rs.getFloat("float1");
                    double1 = rs.getDouble("double1");
                    name = rs.getString("name");
                    countRows++;
                }
                assertEquals(2, countRows);
                assertEquals(boolean1, true);
                assertEquals(short1, (short) 1);
                assertEquals(integer1, 1);
                assertEquals(long1, 1L);
                assertEquals(float1, 1F, 0);
                assertEquals(double1, 1D, 0);
                assertEquals("marko", name);
                rs.close();
            }
        }
    }

    @Test
    public void testAndColumns() {
        Vertex v1 = this.sqlG.addVertex(Element.LABEL, "Person", "name1", "marko");
        this.sqlG.tx().commit();
        assertEquals(1, this.sqlG.V().count().next(), 0);
        assertEquals(v1, this.sqlG.v(v1.id()));
        assertEquals(1, v1.properties().count().next(), 0);
        Vertex v2 = this.sqlG.addVertex(Element.LABEL, "Person", "name2", "john");
        assertEquals(2, this.sqlG.V().count().next(), 0);
        assertEquals(v2, this.sqlG.v(v2.id()));
        assertEquals(1, v2.properties().count().next(), 0);
    }

}

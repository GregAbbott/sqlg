package org.umlg.sqlg.test.localvertexstep;

import org.apache.tinkerpop.gremlin.process.traversal.Order;
import org.apache.tinkerpop.gremlin.process.traversal.Path;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.DefaultGraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.OrderGlobalStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.PathStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.sideEffect.IdentityStep;
import org.apache.tinkerpop.gremlin.process.traversal.util.TraversalHelper;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Assert;
import org.junit.Test;
import org.umlg.sqlg.step.SqlgVertexStep;
import org.umlg.sqlg.step.SqlgGraphStep;
import org.umlg.sqlg.step.SqlgLocalStep;
import org.umlg.sqlg.test.BaseTest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Date: 2017/04/05
 * Time: 12:29 PM
 */
public class TestLocalVertexStepOptionalWithOrder extends BaseTest {

    @Test
    public void testSimple() {
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "order", 1);
        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "order", 1);
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B", "order", 2);
        Vertex b3 = this.sqlgGraph.addVertex(T.label, "B", "order", 3);
        a1.addEdge("ab", b1);
        a1.addEdge("ab", b2);
        a1.addEdge("ab", b3);
        this.sqlgGraph.tx().commit();
        DefaultGraphTraversal<Vertex, Vertex> traversal = (DefaultGraphTraversal<Vertex, Vertex>) this.sqlgGraph.traversal()
                .V().hasLabel("A")
                .local(
                        __.out().order().by("order", Order.decr)
                );
        Assert.assertEquals(3, traversal.getSteps().size());
        List<Vertex> vertices = traversal.toList();
        Assert.assertEquals(2, traversal.getSteps().size());
        Assert.assertTrue(traversal.getSteps().get(1) instanceof SqlgLocalStep);
        SqlgLocalStep<?, ?> localStep = (SqlgLocalStep<?, ?>) traversal.getSteps().get(1);

        List<SqlgVertexStep> sqlgVertexSteps = TraversalHelper.getStepsOfAssignableClassRecursively(SqlgVertexStep.class, localStep.getLocalChildren().get(0));
        Assert.assertEquals(1, sqlgVertexSteps.size());
        SqlgVertexStep sqlgVertexStep = sqlgVertexSteps.get(0);
        assertStep(sqlgVertexStep, false, false, false, false);
        Assert.assertEquals(3, vertices.size());
        Assert.assertEquals(b3, vertices.get(0));
        Assert.assertEquals(b2, vertices.get(1));
        Assert.assertEquals(b1, vertices.get(2));
    }

    @Test
    public void testSimpleButLessSo() {
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "order", 1);
        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "order", 1);
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B", "order", 2);
        Vertex b3 = this.sqlgGraph.addVertex(T.label, "B", "order", 3);
        Vertex a12 = this.sqlgGraph.addVertex(T.label, "A", "order", 10);
        Vertex b12 = this.sqlgGraph.addVertex(T.label, "B", "order", 1);
        Vertex b22 = this.sqlgGraph.addVertex(T.label, "B", "order", 2);
        Vertex b32 = this.sqlgGraph.addVertex(T.label, "B", "order", 3);
        a1.addEdge("ab", b1);
        a1.addEdge("ab", b2);
        a1.addEdge("ab", b3);
        a12.addEdge("ab", b12);
        a12.addEdge("ab", b22);
        a12.addEdge("ab", b32);
        this.sqlgGraph.tx().commit();
        DefaultGraphTraversal<Vertex, Vertex> traversal = (DefaultGraphTraversal<Vertex, Vertex>) this.sqlgGraph.traversal()
                .V().hasLabel("A").as("a")
                .local(
                        __.out().as("b")
                )
                .order()
                .by(__.select("a").by("order"), Order.decr)
                .by(__.select("b").by("order"), Order.decr);
        Assert.assertEquals(4, traversal.getSteps().size());
        List<Vertex> vertices = traversal.toList();
        Assert.assertEquals(4, traversal.getSteps().size());
        Assert.assertTrue(traversal.getSteps().get(2) instanceof SqlgLocalStep);
        SqlgLocalStep<?, ?> localStep = (SqlgLocalStep<?, ?>) traversal.getSteps().get(2);
        List<SqlgVertexStep> sqlgVertexSteps = TraversalHelper.getStepsOfAssignableClassRecursively(SqlgVertexStep.class, localStep.getLocalChildren().get(0));
        Assert.assertEquals(1, sqlgVertexSteps.size());
        SqlgVertexStep sqlgVertexStep = sqlgVertexSteps.get(0);
        assertStep(sqlgVertexStep, false, false, false, true);
        Assert.assertEquals(6, vertices.size());
        Assert.assertEquals(b32, vertices.get(0));
        Assert.assertEquals(b22, vertices.get(1));
        Assert.assertEquals(b12, vertices.get(2));
        Assert.assertEquals(b3, vertices.get(3));
        Assert.assertEquals(b2, vertices.get(4));
        Assert.assertEquals(b1, vertices.get(5));
    }

    @Test
    public void testOptionalWithOrder() {
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "name", "a");
        Vertex a2 = this.sqlgGraph.addVertex(T.label, "A", "name", "aa");
        Vertex a3 = this.sqlgGraph.addVertex(T.label, "A", "name", "aaa");

        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "name", "d");
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B", "name", "c");
        Vertex b3 = this.sqlgGraph.addVertex(T.label, "B", "name", "b");
        Vertex bb1 = this.sqlgGraph.addVertex(T.label, "BB", "name", "g");
        Vertex bb2 = this.sqlgGraph.addVertex(T.label, "BB", "name", "f");
        Vertex bb3 = this.sqlgGraph.addVertex(T.label, "BB", "name", "e");

        Vertex c1 = this.sqlgGraph.addVertex(T.label, "C", "name", "h");
        Vertex c2 = this.sqlgGraph.addVertex(T.label, "C", "name", "i");
        Vertex c3 = this.sqlgGraph.addVertex(T.label, "C", "name", "j");
        Vertex cc1 = this.sqlgGraph.addVertex(T.label, "CC", "name", "k");
        Vertex cc2 = this.sqlgGraph.addVertex(T.label, "CC", "name", "l");
        Vertex cc3 = this.sqlgGraph.addVertex(T.label, "CC", "name", "m");

        a1.addEdge("ab", b1);
        a1.addEdge("ab", b2);
        a1.addEdge("ab", b3);
        a1.addEdge("abb", bb1);
        a1.addEdge("abb", bb2);
        a1.addEdge("abb", bb3);

        b1.addEdge("bc", c1);
        b1.addEdge("bc", c2);
        b1.addEdge("bc", c3);
        b2.addEdge("bcc", cc1);
        b2.addEdge("bcc", cc2);
        b2.addEdge("bcc", cc3);
        this.sqlgGraph.tx().commit();

        DefaultGraphTraversal<Vertex, Path> traversal = (DefaultGraphTraversal<Vertex, Path>) this.sqlgGraph.traversal()
                .V().hasLabel("A")
                .local(
                        __.optional(
                                __.out().order().by("name").optional(
                                        __.out().order().by("name", Order.decr)
                                )
                        )
                )
                .path();
        Assert.assertEquals(4, traversal.getSteps().size());
        List<Path> paths = traversal.toList();
        Assert.assertEquals(3, traversal.getSteps().size());
        Assert.assertTrue(traversal.getSteps().get(1) instanceof SqlgLocalStep);
        SqlgLocalStep<?, ?> localStep = (SqlgLocalStep<?, ?>) traversal.getSteps().get(1);
        List<SqlgVertexStep> sqlgVertexSteps = TraversalHelper.getStepsOfAssignableClassRecursively(SqlgVertexStep.class, localStep.getLocalChildren().get(0));
        Assert.assertEquals(1, sqlgVertexSteps.size());
        SqlgVertexStep sqlgVertexStep = sqlgVertexSteps.get(0);
        assertStep(sqlgVertexStep, false, true, true, true);

        Assert.assertEquals(12, paths.size());

        //assert the order
        //all the paths of length 2 and 3 must be sorted
        List<Path> pathsOfLength3 = paths.stream().filter(p -> p.size() == 3).collect(Collectors.toList());
        Vertex v = (Vertex) pathsOfLength3.get(5).objects().get(2);
        Assert.assertEquals("h", v.value("name"));
        v = (Vertex) pathsOfLength3.get(4).objects().get(2);
        Assert.assertEquals("i", v.value("name"));
        v = (Vertex) pathsOfLength3.get(3).objects().get(2);
        Assert.assertEquals("j", v.value("name"));
        v = (Vertex) pathsOfLength3.get(2).objects().get(2);
        Assert.assertEquals("k", v.value("name"));
        v = (Vertex) pathsOfLength3.get(1).objects().get(2);
        Assert.assertEquals("l", v.value("name"));
        v = (Vertex) pathsOfLength3.get(0).objects().get(2);
        Assert.assertEquals("m", v.value("name"));

        List<Path> pathsOfLength2 = paths.stream().filter(p -> p.size() == 2).collect(Collectors.toList());
        v = (Vertex) pathsOfLength2.get(0).objects().get(1);
        Assert.assertEquals("b", v.value("name"));
        v = (Vertex) pathsOfLength2.get(1).objects().get(1);
        Assert.assertEquals("e", v.value("name"));
        v = (Vertex) pathsOfLength2.get(2).objects().get(1);
        Assert.assertEquals("f", v.value("name"));
        v = (Vertex) pathsOfLength2.get(3).objects().get(1);
        Assert.assertEquals("g", v.value("name"));

        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b1) && p.get(2).equals(c1)));
        paths.remove(paths.stream().filter(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b1) && p.get(2).equals(c1)).findAny().get());
        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b1) && p.get(2).equals(c2)));
        paths.remove(paths.stream().filter(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b1) && p.get(2).equals(c2)).findAny().get());
        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b1) && p.get(2).equals(c3)));
        paths.remove(paths.stream().filter(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b1) && p.get(2).equals(c3)).findAny().get());
        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b2) && p.get(2).equals(cc1)));
        paths.remove(paths.stream().filter(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b2) && p.get(2).equals(cc1)).findAny().get());
        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b2) && p.get(2).equals(cc2)));
        paths.remove(paths.stream().filter(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b2) && p.get(2).equals(cc2)).findAny().get());
        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b2) && p.get(2).equals(cc3)));
        paths.remove(paths.stream().filter(p -> p.size() == 3 && p.get(0).equals(a1) && p.get(1).equals(b2) && p.get(2).equals(cc3)).findAny().get());

        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 2 && p.get(0).equals(a1) && p.get(1).equals(bb1)));
        paths.remove(paths.stream().filter(p -> p.size() == 2 && p.get(0).equals(a1) && p.get(1).equals(bb1)).findAny().get());
        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 2 && p.get(0).equals(a1) && p.get(1).equals(bb2)));
        paths.remove(paths.stream().filter(p -> p.size() == 2 && p.get(0).equals(a1) && p.get(1).equals(bb2)).findAny().get());
        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 2 && p.get(0).equals(a1) && p.get(1).equals(bb3)));
        paths.remove(paths.stream().filter(p -> p.size() == 2 && p.get(0).equals(a1) && p.get(1).equals(bb3)).findAny().get());

        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 2 && p.get(0).equals(a1) && p.get(1).equals(b3)));
        paths.remove(paths.stream().filter(p -> p.size() == 2 && p.get(0).equals(a1) && p.get(1).equals(b3)).findAny().get());

        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 1 && p.get(0).equals(a2)));
        paths.remove(paths.stream().filter(p -> p.size() == 1 && p.get(0).equals(a2)).findAny().get());
        Assert.assertTrue(paths.stream().anyMatch(p -> p.size() == 1 && p.get(0).equals(a3)));
        paths.remove(paths.stream().filter(p -> p.size() == 1 && p.get(0).equals(a3)).findAny().get());
        Assert.assertTrue(paths.isEmpty());
    }

    @Test
    public void testOptionalWithOrderAndRange() {
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "name", "a");
        Vertex a2 = this.sqlgGraph.addVertex(T.label, "A", "name", "aa");
        Vertex a3 = this.sqlgGraph.addVertex(T.label, "A", "name", "aaa");

        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "name", "d");
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B", "name", "c");
        Vertex b3 = this.sqlgGraph.addVertex(T.label, "B", "name", "b");
        Vertex bb1 = this.sqlgGraph.addVertex(T.label, "BB", "name", "g");
        Vertex bb2 = this.sqlgGraph.addVertex(T.label, "BB", "name", "f");
        Vertex bb3 = this.sqlgGraph.addVertex(T.label, "BB", "name", "e");

        Vertex c1 = this.sqlgGraph.addVertex(T.label, "C", "name", "h");
        Vertex c2 = this.sqlgGraph.addVertex(T.label, "C", "name", "i");
        Vertex c3 = this.sqlgGraph.addVertex(T.label, "C", "name", "j");
        Vertex cc1 = this.sqlgGraph.addVertex(T.label, "CC", "name", "k");
        Vertex cc2 = this.sqlgGraph.addVertex(T.label, "CC", "name", "l");
        Vertex cc3 = this.sqlgGraph.addVertex(T.label, "CC", "name", "m");

        a1.addEdge("ab", b1);
        a1.addEdge("ab", b2);
        a1.addEdge("ab", b3);
        a1.addEdge("abb", bb1);
        a1.addEdge("abb", bb2);
        a1.addEdge("abb", bb3);

        b1.addEdge("bc", c1);
        b1.addEdge("bc", c2);
        b1.addEdge("bc", c3);
        b2.addEdge("bcc", cc1);
        b2.addEdge("bcc", cc2);
        b2.addEdge("bcc", cc3);
        this.sqlgGraph.tx().commit();

        DefaultGraphTraversal<Vertex, Path> traversal = (DefaultGraphTraversal<Vertex, Path>) this.sqlgGraph.traversal()
                .V().hasLabel("A")
                .local(
                        __.optional(
                                __.out().order().by("name").optional(
                                        __.out().order().by("name", Order.decr).range(2, 3)
                                )
                        )
                )
                .path();
        Assert.assertEquals(4, traversal.getSteps().size());
        List<Path> paths = traversal.toList();
        Assert.assertEquals(3, traversal.getSteps().size());
        Assert.assertTrue(traversal.getSteps().get(1) instanceof SqlgLocalStep);
        SqlgLocalStep<?, ?> localStep = (SqlgLocalStep<?, ?>) traversal.getSteps().get(1);
        List<SqlgVertexStep> sqlgVertexSteps = TraversalHelper.getStepsOfAssignableClassRecursively(SqlgVertexStep.class, localStep.getLocalChildren().get(0));
        Assert.assertEquals(1, sqlgVertexSteps.size());
        SqlgVertexStep sqlgVertexStep = sqlgVertexSteps.get(0);
        assertStep(sqlgVertexStep, false, true, true, true);

        Assert.assertEquals(7, paths.size());

        //all the paths of length 2 and 3 must be sorted
        List<Path> pathsOfLength3 = paths.stream().filter(p -> p.size() == 3).collect(Collectors.toList());
        Assert.assertEquals(1, pathsOfLength3.size());
        Vertex v = (Vertex) pathsOfLength3.get(0).objects().get(2);
        Assert.assertEquals("k", v.value("name"));

        List<Path> pathsOfLength2 = paths.stream().filter(p -> p.size() == 2).collect(Collectors.toList());
        Assert.assertEquals(4, pathsOfLength2.size());
        v = (Vertex) pathsOfLength2.get(0).objects().get(1);
        Assert.assertEquals("b", v.value("name"));
        v = (Vertex) pathsOfLength2.get(1).objects().get(1);
        Assert.assertEquals("e", v.value("name"));
        v = (Vertex) pathsOfLength2.get(2).objects().get(1);
        Assert.assertEquals("f", v.value("name"));
        v = (Vertex) pathsOfLength2.get(3).objects().get(1);
        Assert.assertEquals("g", v.value("name"));

        List<Path> pathsOfLength1 = paths.stream().filter(p -> p.size() == 1).collect(Collectors.toList());
        Assert.assertEquals(2, pathsOfLength1.size());
    }

    @Test
    public void testOptionalWithOrderAndRange2() {
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "name", "a");
        Vertex a2 = this.sqlgGraph.addVertex(T.label, "A", "name", "aa");
        Vertex a3 = this.sqlgGraph.addVertex(T.label, "A", "name", "aaa");

        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "name", "d");
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B", "name", "c");
        Vertex b3 = this.sqlgGraph.addVertex(T.label, "B", "name", "b");
        Vertex bb1 = this.sqlgGraph.addVertex(T.label, "BB", "name", "g");
        Vertex bb2 = this.sqlgGraph.addVertex(T.label, "BB", "name", "f");
        Vertex bb3 = this.sqlgGraph.addVertex(T.label, "BB", "name", "e");

        Vertex c1 = this.sqlgGraph.addVertex(T.label, "C", "name", "h");
        Vertex c2 = this.sqlgGraph.addVertex(T.label, "C", "name", "i");
        Vertex c3 = this.sqlgGraph.addVertex(T.label, "C", "name", "j");
        Vertex cc1 = this.sqlgGraph.addVertex(T.label, "CC", "name", "k");
        Vertex cc2 = this.sqlgGraph.addVertex(T.label, "CC", "name", "l");
        Vertex cc3 = this.sqlgGraph.addVertex(T.label, "CC", "name", "m");

        a1.addEdge("ab", b1);
        a1.addEdge("ab", b2);
        a1.addEdge("ab", b3);
        a1.addEdge("abb", bb1);
        a1.addEdge("abb", bb2);
        a1.addEdge("abb", bb3);

        b1.addEdge("bc", c1);
        b1.addEdge("bc", c2);
        b1.addEdge("bc", c3);
        b2.addEdge("bcc", cc1);
        b2.addEdge("bcc", cc2);
        b2.addEdge("bcc", cc3);
        this.sqlgGraph.tx().commit();

        DefaultGraphTraversal<Vertex, Path> traversal = (DefaultGraphTraversal<Vertex, Path>) this.sqlgGraph.traversal()
                .V().hasLabel("A")
                .local(
                        __.optional(
                                __.out().order().by("name").range(1, 2).optional(
                                        __.out().order().by("name", Order.decr).range(2, 3)
                                )
                        )
                )
                .path();
        Assert.assertEquals(4, traversal.getSteps().size());
        List<Path> paths = traversal.toList();
        //This query is no fully optimized.
        //The range messes it up, so it has a SqlgVertexStep
        Assert.assertEquals(3, traversal.getSteps().size());
        Assert.assertTrue(traversal.getSteps().get(1) instanceof SqlgLocalStep);
        SqlgLocalStep<?, ?> localStep = (SqlgLocalStep<?, ?>) traversal.getSteps().get(1);
        List<SqlgVertexStep> sqlgVertexSteps = TraversalHelper.getStepsOfAssignableClassRecursively(SqlgVertexStep.class, localStep.getLocalChildren().get(0));
        Assert.assertEquals(6, sqlgVertexSteps.size());

        Assert.assertEquals(3, paths.size());
        List<Path> pathsOfLength1 = paths.stream().filter(p -> p.size() == 1).collect(Collectors.toList());
        Assert.assertEquals(2, pathsOfLength1.size());

        List<Path> pathsOfLength3 = paths.stream().filter(p -> p.size() == 3).collect(Collectors.toList());
        Assert.assertEquals(1, pathsOfLength3.size());

        Vertex v = (Vertex) pathsOfLength3.get(0).objects().get(2);
        Assert.assertEquals("k", v.value("name"));
    }

    @Test
    public void testOptionalWithOrder2() {
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "order", 13);
        Vertex a2 = this.sqlgGraph.addVertex(T.label, "A", "order", 12);
        Vertex a3 = this.sqlgGraph.addVertex(T.label, "A", "order", 11);
        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "order", 3);
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B", "order", 2);
        Vertex b3 = this.sqlgGraph.addVertex(T.label, "B", "order", 1);
        a1.addEdge("ab", b1);
        a1.addEdge("ab", b2);
        a1.addEdge("ab", b3);
        this.sqlgGraph.tx().commit();

        DefaultGraphTraversal<Vertex, Vertex> traversal = (DefaultGraphTraversal<Vertex, Vertex>) this.sqlgGraph.traversal()
                .V().hasLabel("A").as("a")
                .local(
                        __.optional(
                                __.outE().as("e").otherV().as("v")
                        )
                ).order().by("order");
        Assert.assertEquals(4, traversal.getSteps().size());
        List<Vertex> vertices = traversal.toList();
        Assert.assertEquals(4, traversal.getSteps().size());

        Assert.assertTrue(traversal.getSteps().get(0) instanceof SqlgGraphStep);
        Assert.assertTrue(traversal.getSteps().get(1) instanceof IdentityStep);
        Assert.assertTrue(traversal.getSteps().get(2) instanceof SqlgLocalStep);
        Assert.assertTrue(traversal.getSteps().get(3) instanceof OrderGlobalStep);
        SqlgLocalStep<?, ?> localStep = (SqlgLocalStep<?, ?>) traversal.getSteps().get(2);
        List<SqlgVertexStep> sqlgVertexSteps = TraversalHelper.getStepsOfAssignableClassRecursively(SqlgVertexStep.class, localStep.getLocalChildren().get(0));
        Assert.assertEquals(1, sqlgVertexSteps.size());

        Assert.assertEquals(5, vertices.size());
        Assert.assertEquals(b3, vertices.get(0));
        Assert.assertEquals(b2, vertices.get(1));
        Assert.assertEquals(b1, vertices.get(2));
        Assert.assertEquals(a3, vertices.get(3));
        Assert.assertEquals(a2, vertices.get(4));
    }

    @Test
    public void testOptionalWithOrderBy2() {
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "name", "a1");
        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "name", "b1");
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B", "name", "b2");
        Vertex b3 = this.sqlgGraph.addVertex(T.label, "B", "name", "b3");
        Vertex c1 = this.sqlgGraph.addVertex(T.label, "C", "name", "c1");
        Vertex c2 = this.sqlgGraph.addVertex(T.label, "C", "name", "c2");
        Vertex c3 = this.sqlgGraph.addVertex(T.label, "C", "name", "c3");
        Vertex d1 = this.sqlgGraph.addVertex(T.label, "D", "name", "d1");
        Vertex d2 = this.sqlgGraph.addVertex(T.label, "D", "name", "d2");
        Vertex d3 = this.sqlgGraph.addVertex(T.label, "D", "name", "d3");
        Edge ab1 = a1.addEdge("ab", b1, "order", 3);
        Edge ab2 = a1.addEdge("ab", b2, "order", 2);
        Edge ab3 = a1.addEdge("ab", b3, "order", 1);
        Edge bc1 = b1.addEdge("bc", c1, "order", 3);
        Edge bc2 = b1.addEdge("bc", c2, "order", 2);
        Edge bc3 = b1.addEdge("bc", c3, "order", 1);
        Edge cd1 = c1.addEdge("cd", d1, "order", 3);
        Edge cd2 = c1.addEdge("cd", d2, "order", 2);
        Edge cd3 = c1.addEdge("cd", d3, "order", 1);
        this.sqlgGraph.tx().commit();
        DefaultGraphTraversal<Vertex, Path> traversal = (DefaultGraphTraversal<Vertex, Path>) this.sqlgGraph.traversal().V(a1.id())
                .local(
                        __.optional(
                                __.outE("ab").as("ab").otherV().as("vb").order().by(__.select("ab").by("order"), Order.incr)
                                        .optional(
                                                __.outE("bc").as("bc").otherV().as("vc").order().by(__.select("bc").by("order"), Order.incr)
                                                        .optional(
                                                                __.outE("cd").as("cd").inV().as("vd").order().by(__.select("cd").by("order"), Order.incr)
                                                        )
                                        )
                        )
                )
                .path();
        Assert.assertEquals(3, traversal.getSteps().size());
        List<Path> paths = traversal.toList();
        Assert.assertEquals(3, traversal.getSteps().size());

        Assert.assertTrue(traversal.getSteps().get(0) instanceof SqlgGraphStep);
        Assert.assertTrue(traversal.getSteps().get(1) instanceof SqlgLocalStep);
        Assert.assertTrue(traversal.getSteps().get(2) instanceof PathStep);
        SqlgLocalStep<?, ?> localStep = (SqlgLocalStep<?, ?>) traversal.getSteps().get(1);
        List<SqlgVertexStep> sqlgVertexSteps = TraversalHelper.getStepsOfAssignableClassRecursively(SqlgVertexStep.class, localStep.getLocalChildren().get(0));
        Assert.assertEquals(1, sqlgVertexSteps.size());

        Assert.assertEquals(7, paths.size());
        Assert.assertTrue(paths.stream().anyMatch(
                p -> p.size() == 5 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab1) &&
                        p.get(2).equals(b1) &&
                        p.get(3).equals(bc3) &&
                        p.get(4).equals(c3)
        ));
        paths.remove(paths.stream().filter(
                p -> p.size() == 5 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab1) &&
                        p.get(2).equals(b1) &&
                        p.get(3).equals(bc3) &&
                        p.get(4).equals(c3)
        ).findAny().get());
        Assert.assertEquals(6, paths.size());

        Assert.assertTrue(paths.stream().anyMatch(
                p -> p.size() == 5 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab1) &&
                        p.get(2).equals(b1) &&
                        p.get(3).equals(bc2) &&
                        p.get(4).equals(c2)
        ));
        paths.remove(paths.stream().filter(
                p -> p.size() == 5 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab1) &&
                        p.get(2).equals(b1) &&
                        p.get(3).equals(bc2) &&
                        p.get(4).equals(c2)
        ).findAny().get());
        Assert.assertEquals(5, paths.size());

        Assert.assertTrue(paths.stream().anyMatch(
                p -> p.size() == 7 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab1) &&
                        p.get(2).equals(b1) &&
                        p.get(3).equals(bc1) &&
                        p.get(4).equals(c1) &&
                        p.get(5).equals(cd1) &&
                        p.get(6).equals(d1)
        ));
        paths.remove(paths.stream().filter(
                p -> p.size() == 7 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab1) &&
                        p.get(2).equals(b1) &&
                        p.get(3).equals(bc1) &&
                        p.get(4).equals(c1) &&
                        p.get(5).equals(cd1) &&
                        p.get(6).equals(d1)
        ).findAny().get());
        Assert.assertEquals(4, paths.size());

        Assert.assertTrue(paths.stream().anyMatch(
                p -> p.size() == 7 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab1) &&
                        p.get(2).equals(b1) &&
                        p.get(3).equals(bc1) &&
                        p.get(4).equals(c1) &&
                        p.get(5).equals(cd2) &&
                        p.get(6).equals(d2)
        ));
        paths.remove(paths.stream().filter(
                p -> p.size() == 7 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab1) &&
                        p.get(2).equals(b1) &&
                        p.get(3).equals(bc1) &&
                        p.get(4).equals(c1) &&
                        p.get(5).equals(cd2) &&
                        p.get(6).equals(d2)
        ).findAny().get());
        Assert.assertEquals(3, paths.size());

        Assert.assertTrue(paths.stream().anyMatch(
                p -> p.size() == 7 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab1) &&
                        p.get(2).equals(b1) &&
                        p.get(3).equals(bc1) &&
                        p.get(4).equals(c1) &&
                        p.get(5).equals(cd3) &&
                        p.get(6).equals(d3)
        ));
        paths.remove(paths.stream().filter(
                p -> p.size() == 7 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab1) &&
                        p.get(2).equals(b1) &&
                        p.get(3).equals(bc1) &&
                        p.get(4).equals(c1) &&
                        p.get(5).equals(cd3) &&
                        p.get(6).equals(d3)
        ).findAny().get());
        Assert.assertEquals(2, paths.size());

        Assert.assertTrue(paths.stream().anyMatch(
                p -> p.size() == 3 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab2) &&
                        p.get(2).equals(b2)
        ));
        paths.remove(paths.stream().filter(
                p -> p.size() == 3 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab2) &&
                        p.get(2).equals(b2)
        ).findAny().get());
        Assert.assertEquals(1, paths.size());

        Assert.assertTrue(paths.stream().anyMatch(
                p -> p.size() == 3 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab3) &&
                        p.get(2).equals(b3)
        ));
        paths.remove(paths.stream().filter(
                p -> p.size() == 3 &&
                        p.get(0).equals(a1) &&
                        p.get(1).equals(ab3) &&
                        p.get(2).equals(b3)
        ).findAny().get());
        Assert.assertEquals(0, paths.size());
    }

    @Test
    public void testVertexStepAfterOptional() {
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "order", 1);
        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "order", 1);
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B", "order", 1);
        Vertex b3 = this.sqlgGraph.addVertex(T.label, "B", "order", 1);
        a1.addEdge("ab", b1);
        a1.addEdge("ab", b2);
        a1.addEdge("ab", b3);
        Vertex c1 = this.sqlgGraph.addVertex(T.label, "C", "order", 1);
        Vertex c2 = this.sqlgGraph.addVertex(T.label, "C", "order", 1);
        Vertex c3 = this.sqlgGraph.addVertex(T.label, "C", "order", 1);
        b1.addEdge("bc", c1);
        b1.addEdge("bc", c2);
        b1.addEdge("bc", c3);
        Vertex d1 = this.sqlgGraph.addVertex(T.label, "D", "order", 1);
        Vertex d2 = this.sqlgGraph.addVertex(T.label, "D", "order", 2);
        Vertex d3 = this.sqlgGraph.addVertex(T.label, "D", "order", 3);
        c1.addEdge("cd", d1);
        c1.addEdge("cd", d2);
        c1.addEdge("cd", d3);

        Vertex a2 = this.sqlgGraph.addVertex(T.label, "A", "order", 1);
        Vertex bb1 = this.sqlgGraph.addVertex(T.label, "BB", "order", -1);
        Vertex bb2 = this.sqlgGraph.addVertex(T.label, "BB", "order", -2);
        Vertex bb3 = this.sqlgGraph.addVertex(T.label, "BB", "order", -3);
        a2.addEdge("abb", bb1);
        a2.addEdge("abb", bb2);
        a2.addEdge("abb", bb3);
        this.sqlgGraph.tx().commit();

        DefaultGraphTraversal<Vertex, Vertex> traversal = (DefaultGraphTraversal<Vertex, Vertex>) this.sqlgGraph.traversal().V().hasLabel("A")
                .local(
                        __.optional(
                                __.out("ab")
                                        .optional(
                                                __.out()
                                        )
                        )
                )
                .out()
                .order().by("order", Order.decr);

        Assert.assertEquals(5, traversal.getSteps().size());
        List<Vertex> vertices = traversal.toList();
        Assert.assertEquals(3, traversal.getSteps().size());

        Assert.assertTrue(traversal.getSteps().get(0) instanceof SqlgGraphStep);
        Assert.assertTrue(traversal.getSteps().get(1) instanceof SqlgLocalStep);
        Assert.assertTrue(traversal.getSteps().get(2) instanceof SqlgVertexStep);
        SqlgLocalStep<?, ?> localStep = (SqlgLocalStep<?, ?>) traversal.getSteps().get(1);
        List<SqlgVertexStep> sqlgVertexSteps = TraversalHelper.getStepsOfAssignableClassRecursively(SqlgVertexStep.class, localStep.getLocalChildren().get(0));
        Assert.assertEquals(1, sqlgVertexSteps.size());

        Assert.assertEquals(6, vertices.size());
        Assert.assertEquals(d3, vertices.get(0));
        Assert.assertEquals(d2, vertices.get(1));
        Assert.assertEquals(d1, vertices.get(2));
        Assert.assertEquals(bb1, vertices.get(3));
        Assert.assertEquals(bb2, vertices.get(4));
        Assert.assertEquals(bb3, vertices.get(5));

    }

    /**
     * optionals wrapped in local steps.
     * The idea is that each traversed out/in edge needs to be sorted
     */
    @Test
    public void testUmlgRequirement() {
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "name", "a1");
        Vertex a2 = this.sqlgGraph.addVertex(T.label, "A", "name", "a2");
        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "name", "b1");
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B", "name", "b2");
        Vertex b3 = this.sqlgGraph.addVertex(T.label, "B", "name", "b3");
        Vertex bb1 = this.sqlgGraph.addVertex(T.label, "BB", "name", "bb1");
        Vertex bb2 = this.sqlgGraph.addVertex(T.label, "BB", "name", "bb2");
        Vertex bb3 = this.sqlgGraph.addVertex(T.label, "BB", "name", "bb3");
        a1.addEdge("ab", b1, "order", 3);
        a1.addEdge("ab", b2, "order", 2);
        a1.addEdge("ab", b3, "order", 1);
        a1.addEdge("abb", bb1, "order", 3);
        a1.addEdge("abb", bb2, "order", 2);
        a1.addEdge("abb", bb3, "order", 1);
        Vertex c1 = this.sqlgGraph.addVertex(T.label, "C", "name", "c1");
        Vertex c2 = this.sqlgGraph.addVertex(T.label, "C", "name", "c2");
        Vertex c3 = this.sqlgGraph.addVertex(T.label, "C", "name", "c3");
        b1.addEdge("bc", c1, "order", 1);
        b1.addEdge("bc", c2, "order", 2);
        b1.addEdge("bc", c3, "order", 3);
        Vertex cc1 = this.sqlgGraph.addVertex(T.label, "CC", "name", "cc1");
        Vertex cc2 = this.sqlgGraph.addVertex(T.label, "CC", "name", "cc2");
        Vertex cc3 = this.sqlgGraph.addVertex(T.label, "CC", "name", "cc3");
        b1.addEdge("bcc", cc1, "order", 3);
        b1.addEdge("bcc", cc2, "order", 2);
        b1.addEdge("bcc", cc3, "order", 1);

        DefaultGraphTraversal<Vertex, Path> traversal = (DefaultGraphTraversal<Vertex, Path>) sqlgGraph.traversal()
                .V().hasLabel("A").as("a")
                .local(
                        __.optional(
                                __.outE().as("e1").inV().as("b").order().by(__.select("a").by(T.id)).by(T.label).by(__.select("e1").by("order"))
                                        .local(
                                                __.optional(
                                                        __.outE().as("e2").inV().order().by(__.select("b").by(T.id)).by(T.label).by(__.select("e2").by("order"))
                                                )
                                        )
                        )
                )
                .path();


        Assert.assertEquals(4, traversal.getSteps().size());
        List<Path> paths = traversal.toList();
        Assert.assertEquals(12, paths.size());
        Path pathX = paths.get(0);
        Assert.assertEquals(3, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(b3));

        pathX = paths.get(1);
        Assert.assertEquals(3, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(b2));

        pathX = paths.get(2);
        Assert.assertEquals(5, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(b1));
        Assert.assertTrue(pathX.get(4).equals(c1));

        pathX = paths.get(3);
        Assert.assertEquals(5, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(b1));
        Assert.assertTrue(pathX.get(4).equals(c2));

        pathX = paths.get(4);
        Assert.assertEquals(5, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(b1));
        Assert.assertTrue(pathX.get(4).equals(c3));

        pathX = paths.get(5);
        Assert.assertEquals(5, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(b1));
        Assert.assertTrue(pathX.get(4).equals(cc3));

        pathX = paths.get(6);
        Assert.assertEquals(5, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(b1));
        Assert.assertTrue(pathX.get(4).equals(cc2));

        pathX = paths.get(7);
        Assert.assertEquals(5, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(b1));
        Assert.assertTrue(pathX.get(4).equals(cc1));

        pathX = paths.get(8);
        Assert.assertEquals(3, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(bb3));

        pathX = paths.get(9);
        Assert.assertEquals(3, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(bb2));

        pathX = paths.get(10);
        Assert.assertEquals(3, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(bb1));

        pathX = paths.get(11);
        Assert.assertEquals(1, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a2));

//        for (Path path : paths) {
//            for (Object v: path.objects()) {
//                Element vertex = (Element)v;
//                if (vertex instanceof Vertex) {
//                    System.out.print(vertex.<String>value("name"));
//                    System.out.print(" - ");
//                }
//            }
//        }
    }

    @Test
    public void testUmlgRequirementWithRange() {
        Vertex a1 = this.sqlgGraph.addVertex(T.label, "A", "name", "a1");
        Vertex a2 = this.sqlgGraph.addVertex(T.label, "A", "name", "a2");
        Vertex b1 = this.sqlgGraph.addVertex(T.label, "B", "name", "b1");
        Vertex b2 = this.sqlgGraph.addVertex(T.label, "B", "name", "b2");
        Vertex b3 = this.sqlgGraph.addVertex(T.label, "B", "name", "b3");
        Vertex bb1 = this.sqlgGraph.addVertex(T.label, "BB", "name", "bb1");
        Vertex bb2 = this.sqlgGraph.addVertex(T.label, "BB", "name", "bb2");
        Vertex bb3 = this.sqlgGraph.addVertex(T.label, "BB", "name", "bb3");
        a1.addEdge("ab", b1, "order", 3);
        a1.addEdge("ab", b2, "order", 2);
        a1.addEdge("ab", b3, "order", 1);
        a1.addEdge("abb", bb1, "order", 3);
        a1.addEdge("abb", bb2, "order", 2);
        a1.addEdge("abb", bb3, "order", 1);
        Vertex c1 = this.sqlgGraph.addVertex(T.label, "C", "name", "c1");
        Vertex c2 = this.sqlgGraph.addVertex(T.label, "C", "name", "c2");
        Vertex c3 = this.sqlgGraph.addVertex(T.label, "C", "name", "c3");
        b1.addEdge("bc", c1, "order", 1);
        b1.addEdge("bc", c2, "order", 2);
        b1.addEdge("bc", c3, "order", 3);
        Vertex cc1 = this.sqlgGraph.addVertex(T.label, "CC", "name", "cc1");
        Vertex cc2 = this.sqlgGraph.addVertex(T.label, "CC", "name", "cc2");
        Vertex cc3 = this.sqlgGraph.addVertex(T.label, "CC", "name", "cc3");
        b1.addEdge("bcc", cc1, "order", 3);
        b1.addEdge("bcc", cc2, "order", 2);
        b1.addEdge("bcc", cc3, "order", 1);

        DefaultGraphTraversal<Vertex, Path> traversal = (DefaultGraphTraversal<Vertex, Path>) sqlgGraph.traversal()
                .V().hasLabel("A").as("a")
                .local(
                        __.optional(
                                __.outE().as("e1").inV().as("b").order().by(__.select("a").by(T.id)).by(T.label).by(__.select("e1").by("order")).limit(1)
                                        .local(
                                                __.optional(
                                                        __.outE().as("e2").inV().order().by(__.select("b").by(T.id)).by(T.label).by(__.select("e2").by("order"))
                                                )
                                        )
                        )
                )
                .path();


        Assert.assertEquals(4, traversal.getSteps().size());
        List<Path> paths = traversal.toList();
        Assert.assertEquals(2, paths.size());
        Path pathX = paths.get(0);
        Assert.assertEquals(3, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a1));
        Assert.assertTrue(pathX.get(2).equals(b3));

        pathX = paths.get(1);
        Assert.assertEquals(1, pathX.size());
        Assert.assertTrue(pathX.get(0).equals(a2));

    }
}

package obp3.traversal.dfs.model;

import obp3.sli.core.IRootedGraph;
import obp3.traversal.dfs.domain.IDepthFirstTraversalConfiguration;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class DepthFirstTraversalParameters<V, A> implements IDepthFirstTraversalParameters<V, A> {

    public DepthFirstTraversalParameters(IRootedGraph<V> graph, Function<V, A> reducer) {
        this(graph, reducer, null, null, null);
    }

    public DepthFirstTraversalParameters(
            IRootedGraph<V> graph,
            Function<V, A> canonize,
            TriFunction<V, V, A, Boolean> onEntry,
            TriFunction<V, V, A, Boolean> onKnown,
            BiFunction<V, IDepthFirstTraversalConfiguration.StackFrame<V>, Boolean> onExit) {
        this.graph = graph;
        this.canonize = canonize;
        this.onEntry = onEntry;
        this.onKnown = onKnown;
        this.onExit = onExit;
    }

    private IRootedGraph<V> graph;

    final TriFunction<V, V, A, Boolean> onEntry;
    final TriFunction<V, V, A, Boolean> onKnown;
    final BiFunction<V, IDepthFirstTraversalConfiguration.StackFrame<V>, Boolean> onExit;

    @Override
    public IRootedGraph<V> getGraph() {
        return graph;
    }

    private Function<V, A> canonize;

    @Override
    public A canonize(V v) {
        return canonize.apply(v);
    }

    @Override
    public boolean hasCallbacks() {
        return onEntry != null || onKnown != null || onExit != null;
    }
    @Override
    public IDepthFirstTraversalCallbacksModel<V, A> callbacks() {
        return new IDepthFirstTraversalCallbacksModel<>() {
            @Override
            public boolean onEntry(V source, V vertex, A canonical) {
                if (onEntry == null) {
                    return false;
                }
                return onEntry.apply(source, vertex, canonical);
            }

            @Override
            public boolean onKnown(V source, V vertex, A canonical) {
                if (onKnown == null) {
                    return false;
                }
                return onKnown.apply(source, vertex, canonical);
            }

            @Override
            public boolean onExit(V vertex, IDepthFirstTraversalConfiguration.StackFrame<V> frame) {
                if (onExit == null) {
                    return false;
                }
                return onExit.apply(vertex, frame);
            }
        };
    }

    public interface TriFunction<I1, I2, I3, O> {
        O apply(I1 i1, I2 i2, I3 i3);

        /**
         * Returns a composed function that first applies this function to
         * its input, and then applies the {@code after} function to the result.
         * If evaluation of either function throws an exception, it is relayed to
         * the caller of the composed function.
         *
         * @param <X> the type of output of the {@code after} function, and of the
         *           composed function
         * @param after the function to apply after this function is applied
         * @return a composed function that first applies this function and then
         * applies the {@code after} function
         * @throws NullPointerException if after is null
         */
        default <X> TriFunction<I1, I2, I3, X> andThen(Function<? super O, ? extends X> after) {
            Objects.requireNonNull(after);
            return (x, y, z) -> after.apply(apply(x, y, z));
        }
    }
}
